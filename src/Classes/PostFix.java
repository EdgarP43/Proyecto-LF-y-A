/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Edgar
 */
public class PostFix {
    private static int precedence(char operator) {
        switch(operator) {
            case '.':
                return 2;
            case '|':
                return 1;
            default:
                return -1;
        }
    }

    public String infixToPostfix(String infixRegex) {
        Deque<Character> stack = new ArrayDeque<Character>();

        String postfixRegex = "";
        for(int i = 0; i < infixRegex.length(); i++) {
            char c = infixRegex.charAt(i);
            switch(c) {
                case '.':
                    while(!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                        postfixRegex += stack.removeFirst();
                    }
                    stack.addFirst(c);
                    break;
                case '|':
                    while(!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                        postfixRegex += stack.removeFirst();
                    }
                    stack.addFirst(c);
                    break;
                case '(':
                    stack.addFirst(c);
                    break;
                case ')':
                    while(!stack.isEmpty() && stack.peek() != '(') {
                        postfixRegex += stack.removeFirst();
                    }
                    stack.removeFirst();
                    break;
                default:
                    postfixRegex += c;
            }
        }

        while(!stack.isEmpty()) {
            postfixRegex += stack.removeFirst();
        }

        return postfixRegex;
    }
}
