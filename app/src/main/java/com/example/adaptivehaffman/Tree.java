package com.example.adaptivehaffman;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

class Tree {
    private HashMap<Character, Node> leaves;
    private List<Node> nodes;

    private Node root;
    private Node esc;

    public static class Node {
        Node Parent;
        Node LeftChild;
        Node RightChild;
        int W;

        Node() { W = 0; }
        Node(Node esc) { Parent = esc; }


        void incWeight() {
            W++;
            if (Parent != null)
                Parent.incWeight();
        }

        void getCode(AtomicReference<String> code) {
            if (Parent == null)
                return;
            if (Parent.LeftChild == this)
                code.set(code.toString() + '0');
            else
                code.set(code.toString() + '1');
            if (Parent.Parent != null)
                Parent.getCode(code);
        }
    }

    private Tree() {
        root = new Node();
        esc = root;

        nodes = new ArrayList<>();
        nodes.add(root);

        leaves = new HashMap<>();
    }

    private void getChar(AtomicReference<Integer> pos, String code, Node node, AtomicReference<String> outStr) throws UnsupportedEncodingException {

        if (node == null || node.LeftChild == null && node.RightChild == null) {
            char ch = decodeChar(code, pos.get());

            if (!leaves.containsValue(node))
                pos.set(pos.get() + 8);
            else {
                // Получаем набор элементов
                Set<Map.Entry<Character, Node>> set = leaves.entrySet();
                for (Map.Entry<Character, Node> me : set) {
                    if (me.getValue() == node)
                        ch = me.getKey();
                }
            }

            AddChar(ch);

            outStr.set(outStr.get() + ch);

            return;
        }

        pos.set(pos.get() + 1);
        if (code.charAt(pos.get() - 1) == '0')
            getChar(pos, code, node.LeftChild, outStr);
        else
            getChar(pos, code, node.RightChild, outStr);
    }

    static String getEncodeString(String inStr) throws UnsupportedEncodingException {
        StringBuilder output = new StringBuilder();
        Tree t = new Tree();

        for (int i = 0; i < inStr.length(); i++) {

            output.append(t.AddChar(inStr.toCharArray()[i]));

            t.sortTree();
            t.recount(t.esc.Parent);
        }

        return output.toString();
    }

    static String getDecodeString(String inStr) throws UnsupportedEncodingException {
        AtomicReference<String> outStr = new AtomicReference<>("");
        AtomicReference<Integer> pos = new AtomicReference<>(0);

        Tree t = new Tree();

        while (pos.get() != inStr.length()) {

            t.getChar(pos, inStr, t.root, outStr);

            t.sortTree();
            t.recount(t.esc.Parent);
        }

        return outStr.get();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static char decodeChar(String inStr, int pos) throws UnsupportedEncodingException {
        if (inStr.length() - pos < 8)
            return '\0';

        int k = Integer.parseInt(inStr.substring(pos, pos+8), 2);

        String rightString = new String(Character.toString((char)k).getBytes(StandardCharsets.ISO_8859_1),"windows-1251");

        return rightString.toCharArray()[0];
    }


    private static String EncodeChar(Character с) throws UnsupportedEncodingException {

        byte[] bytes = с.toString().getBytes("windows-1251");
        StringBuilder binary = new StringBuilder();

        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }

        return binary.toString();
    }

    private String AddChar(char c) throws UnsupportedEncodingException {
        AtomicReference<String> code = new AtomicReference<>("");
        StringBuilder outStr = new StringBuilder();

        if (leaves.containsKey(c)) {
            leaves.get(c).incWeight();

            leaves.get(c).getCode(code);

            outStr.append(code.get());
            outStr = outStr.reverse();

            return outStr.toString();

        } else {
            // leaves[c] = new Node(esc);
            leaves.put(c, new Node(esc));
            // leaves.get(c).c = c;

            Node temp = new Node(esc);

            esc.LeftChild = temp;
            esc.RightChild = leaves.get(c);

            nodes.add(leaves.get(c));
            nodes.add(temp);

            esc.getCode(code);

            outStr.append(code.get());
            outStr = outStr.reverse();
            // code += c;
            outStr.append(EncodeChar(c));

            esc = temp;
            leaves.get(c).incWeight();

            return outStr.toString();
        }
    }

    private void sortTree() {
        boolean isOrdered = false;
        int ind = 0;
        for (int i = nodes.size() - 1; i > 0; i--) {
            for (int j = i - 1; j > -1; j--) {
                if (nodes.get(i).W > nodes.get(j).W) {
                    isOrdered = true;
                    ind = j;
                }
            }
            if (isOrdered) {
                if (nodes.get(i).Parent.LeftChild == nodes.get(i))
                    nodes.get(i).Parent.LeftChild = nodes.get(ind);
                else
                    nodes.get(i).Parent.RightChild = nodes.get(ind);

                if (nodes.get(ind).Parent.LeftChild == nodes.get(ind))
                    nodes.get(ind).Parent.LeftChild = nodes.get(i);
                else
                    nodes.get(ind).Parent.RightChild = nodes.get(i);

                //Swap(nodes[ind].parent, nodes[i].parent);
                Node temp = nodes.get(i).Parent;
                nodes.get(i).Parent = nodes.get(ind).Parent;
                nodes.get(ind).Parent = temp;


                if (nodes.get(i).Parent.LeftChild.W > nodes.get(i).Parent.RightChild.W) {
                    //Swap(nodes[i].parent.leftChild, nodes[i].parent.rightChild);
                    temp = nodes.get(i).Parent.LeftChild;
                    nodes.get(i).Parent.LeftChild = nodes.get(i).Parent.RightChild;
                    nodes.get(i).Parent.RightChild = temp;
                }

                //Swap(nodes[ind], nodes[i]);
                temp = nodes.get(i);
                nodes.set(i, nodes.get(ind));
                nodes.set(ind, temp);

                break;
            }
        }
    }
    private void recount(Node node) {
        if (node != null) {
            node.W = node.LeftChild.W + node.RightChild.W;
            recount(node.Parent);
        }
    }

}
