/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
/**
 *
 * @author Edgar
 */
public class Automata {

    String print_text = "";
    FileManager file = new FileManager();
    PostFix converter = new PostFix();
    boolean foundSet = false;
    int quote_counter = 0;
    String full_expression = "";
    String[] text;
    int sets_mark, tokens_mark, actions_mark;
    HashMap<String, ArrayList<String>> sets = new HashMap();
    HashMap<String, Integer> actions = new HashMap();
    HashMap<Integer, String> leafs = new HashMap();
    HashMap<Integer, ArrayList<Integer>> follows = new HashMap();
    Deque<Node> stack = new ArrayDeque<>();
    ArrayList<String> sets_names = new ArrayList();
    ArrayList<State> states = new ArrayList<>();
    ArrayList<Transition> transitions = new ArrayList();

    public String getPrintText() {
        return print_text;
    }

    public ArrayList<State> getStates() {
        return states;
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    public Automata(String file_name) {
        text = file.readFile(file_name);

        if (file.errors.isEmpty()) {
            sets_mark = file.getSets_mark();
            tokens_mark = file.getTokens_mark();
            actions_mark = file.getActions_mark();

            saveSets();
            saveActions();
            createDFA();

            this.print_text = getDFA();
        } else {
            for (int i = 0; i < file.errors.size(); i++) {
                this.print_text += file.errors.get(i) + "\n";
            }
        }
    }

    public Automata(String new_, int k) {
        full_expression = new_;
        createDFA();
    }

    private void saveSets() {
        int index = sets_mark + 1;
        while (index < tokens_mark) {
            String set_name = "";
            int position = 0;
            char character;
            String s_character;
            //Name of the set
            while (position < text[index].length()) {
                character = text[index].charAt(position);
                s_character = String.valueOf(character);

                position++;
                if (s_character.equals("=")) {
                    break;
                }
                set_name += s_character;
            }

            ArrayList<String> elements = new ArrayList();
            String prev = "";
            String actual = "";
            quote_counter = 0;
            int point_counter = 0;

            if (text[index].contains("chr") || text[index].contains("CHR")) {
                int counter = 0;
                while (position < text[index].length()) {
                    character = text[index].charAt(position);
                    s_character = String.valueOf(character);

                    if (s_character.equals(".")) {
                        point_counter++;
                    } else if (s_character.equals("(") || s_character.equals(")")) {
                        counter++;
                    }

                    if (!(counter % 2 == 0)) {
                        if (!(s_character.equals("("))) {
                            actual += s_character;
                        }

                    } else {
                        if (!(actual.equals(""))) {
                            int num = Integer.valueOf(actual);
                            elements.add(Character.toString((char) num));
                            if ((point_counter % 2 == 0) && point_counter > 0) {
                                elements = (fillSetElements(Integer.valueOf(prev), Integer.valueOf(actual), elements));
                                point_counter = 0;
                                prev = "";
                            }
                            prev = actual;
                            actual = "";
                        }
                    }

                    position++;
                }
            } else {
                while (position < text[index].length()) {
                    character = text[index].charAt(position);
                    s_character = String.valueOf(character);
                    if (s_character.equals("'")) {
                        quote_counter++;
                    } else if (s_character.equals(".")) {
                        point_counter++;
                    }
                    if (!((quote_counter % 2) == 0)) {
                        if (!s_character.equals("'")) {
                            actual += s_character;
                        }
                        if (point_counter >= 0 && s_character.equals(".")) {
                            point_counter--;
                        }
                    } else {
                        if (!actual.equals("")) {
                            elements.add(actual);
                            if ((point_counter % 2 == 0) && point_counter > 0) {
                                elements = (fillSetElements(Integer.valueOf(prev.charAt(0)), Integer.valueOf(actual.charAt(0)), elements));
                                point_counter = 0;
                                prev = "";
                            }
                            prev = actual;
                            actual = "";
                        }
                    }
                    position++;
                }
            }

            sets.put(set_name, elements);
            sets_names.add(set_name);
            index++;
        }
    }

    private void saveActions() {
        int index = actions_mark + 3;
        while (index < text.length) {
            if (text[index].equals("}")) {
                break;
            }

            int position = 0;
            boolean full_name = false;
            boolean identifier = false;
            String chain = "";
            String num = "";
            int quote_counter = 0;

            if (!text[index].equals("")) {
                while (position < text[index].length()) {
                    char character = text[index].charAt(position);
                    String s_character = String.valueOf(character);
                    int char_value = Integer.valueOf(character);

                    if (!full_name) {
                        if ((char_value >= 48 && char_value <= 57)) {
                            num += s_character;
                            identifier = true;
                        } else if (s_character.equals("=")) {
                            if (identifier) {
                                full_name = true;
                            }
                        } 
                    }else{
                        if (s_character.equals("'")) {
                            quote_counter++;
                        }
                        
                        if ((quote_counter % 2 != 0) && !s_character.equals("'")) {
                            chain += s_character;
                        }
                    }
                    
                    position++;
                    if (position == text[index].length()) {
                        actions.put(chain, Integer.valueOf(num));
                    }
                }
            }
            index++;
        }
    }
    private ArrayList<String> fillSetElements(int begin, int end, ArrayList rangeList) {
        while (begin <= end) {
            String new_ = Character.toString((char) begin);
            if (!rangeList.contains(new_)) {
                rangeList.add(new_);
            }
            begin++;
        }
        return rangeList;
    }

    private void createDFA() {
        getFullExpression();
        String postfixExpression = converter.infixToPostfix(full_expression);
        int position = 0;
        quote_counter = 0;
        String chain = "";
        String aux = "";
        boolean no_set = false;
        while (position < postfixExpression.length()) {
            char character = postfixExpression.charAt(position);
            String s_character = String.valueOf(character);

            if (s_character.equals("'")) {
                quote_counter++;
            }

            if (!s_character.equals("'")) {
                chain += s_character;
            }

            if ((quote_counter % 2) == 0) {
                if (no_set) {
                    insertLeaf(aux);
                    aux = "";
                    chain = "";
                    no_set = false;
                } else {
                    if (!searchSet(chain)) {
                        if (isExpressionSymbol(s_character)) {
                            calculateNewNode(s_character);
                        } else {
                            insertLeaf(chain);
                        }
                        chain = "";
                    } else {
                        if (foundSet) {
                            insertLeaf(chain);
                            foundSet = false;
                            chain = "";
                        }
                    }
                }
            } else {
                if (!chain.isEmpty()) {
                    if (searchSet(chain)) {
                        aux = chain;
                        no_set = true;
                    }
                } else if (position < postfixExpression.length() - 1) {
                    if (String.valueOf(postfixExpression.charAt(position + 1)).equals("'") && !no_set) {
                        no_set = true;
                        aux = "'";
                        quote_counter--;
                    }
                }
            }

            position++;
        }

        makeTransitions();
    }

    private void makeTransitions() {
        int state_count = 0;
        ArrayList<State> new_states = new ArrayList<>();
        boolean finish = false;
        State init_state = new State("q" + state_count);
        init_state.init_state = true;
        init_state.setValue(stack.peek().first);

        do {
            states.add(init_state);
            ArrayList<State> temp_states = new ArrayList<>();
            ArrayList<Integer> follow_list;
            State temp;
            for (Integer num : init_state.getValue()) {
                temp = new State();
                String move_chars = leafs.get(num);
                follow_list = new ArrayList<>();
                follow_list = follows.get(num);
                temp.state_name = move_chars;
                if (!move_chars.equals("#")) {
                    if (!temp_states.isEmpty()) {
                        boolean exist = false;
                        int pos = 0;
                        for (int i = 0; i < temp_states.size(); i++) {
                            if (temp_states.get(i).state_name.equals(move_chars)) {
                                exist = true;
                                pos = i;
                                break;
                            }
                        }

                        if (exist) {
                            ArrayList<Integer> temp_list = new ArrayList();
                            temp_list.addAll(temp_states.get(pos).getValue());
                            for (Integer num_2 : follow_list) {
                                if (!temp_list.contains(num_2)) {
                                    temp_list.add(num_2);
                                }
                            }
                            temp_states.get(pos).setValue(temp_list);
                        } else {
                            temp.value = follow_list;
                            temp_states.add(temp);
                        }
                    } else {
                        temp.value = follow_list;
                        temp_states.add(temp);
                    }
                }
            }

            for (int i = 0; i < temp_states.size(); i++) {

                Transition aux_transition = new Transition();
                aux_transition.move = temp_states.get(i).state_name;
                aux_transition.initial_state_name = init_state.state_name;
                if (existInList(states, temp_states.get(i).getValue())) {
                    aux_transition.final_state_name = states.get(searchInList(states, temp_states.get(i).getValue())).state_name;
                } else {
                    if (existInList(new_states, temp_states.get(i).getValue())) {
                        aux_transition.final_state_name = new_states.get(searchInList(new_states, temp_states.get(i).getValue())).state_name;
                    } else {
                        state_count++;
                        aux_transition.final_state_name = "q" + state_count;
                    }
                }
                transitions.add(aux_transition);
                if (!existInList(states, temp_states.get(i).getValue())) {

                    if (!existInList(new_states, temp_states.get(i).getValue())) {
                        temp = temp_states.get(i);
                        temp.state_name = aux_transition.final_state_name;
                        new_states.add(temp);
                    }

                }
            }

            if (!new_states.isEmpty()) {
                init_state = new_states.remove(0);
            } else {
                finish = true;
            }

        } while (!finish);

        for (State state : states) {
            if (state.getValue().contains(leafs.size())) {
                state.final_state = true;
            }
        }

    }

    private void getFullExpression() {
        int index = tokens_mark + 1;
        while (index < actions_mark) {
            String chain = "";
            String aux = "";
            int position = 0;
            for (int i = 0; i < text[index].length(); i++) {
                if (String.valueOf(text[index].charAt(i)).equals("=")) {
                    position = i + 1;
                    break;
                }
            }
            char character;
            String s_character;
            quote_counter = 0;
            while (position < text[index].length()) {
                character = text[index].charAt(position);
                s_character = String.valueOf(character);

                if (s_character.equals("'")) {
                    quote_counter++;
                }

                chain += s_character;
                aux += s_character;

                if ((quote_counter % 2 == 0)) {
                    if (!searchSet(aux)) {
                        if ((quote_counter > 0)) {
                            if (position < text[index].length() - 1) {
                                if (s_character.equals("'") && String.valueOf(text[index].charAt(position + 1)).equals("'")
                                        && String.valueOf(text[index].charAt(position - 1)).equals("'")) {
                                    quote_counter--;
                                } else {
                                    if (!isExpressionSymbol(s_character)) {

                                        if (!isExpressionSymbol(String.valueOf(text[index].charAt(position + 1)))) {
                                            chain += ".";
                                        }

                                    } else {
                                        if (!s_character.equals("(") && !s_character.equals("|")
                                                && (String.valueOf(text[index].charAt(position + 1)).equals("'")
                                                || String.valueOf(text[index].charAt(position + 1)).equals("("))) {
                                            chain += ".";
                                        }
                                    }
                                }
                            }
                            aux = "";
                        }
                    } else {
                        if (foundSet) {
                            if (position < text[index].length() - 1) {
                                if (!isExpressionSymbol(String.valueOf(text[index].charAt(position + 1)))) {
                                    chain += ".";
                                } else if (String.valueOf(text[index].charAt(position + 1)).equals("(")) {
                                    chain += ".";
                                }
                                foundSet = false;
                                aux = "";
                            }
                        }
                    }
                }

                position++;
            }

            if (index == tokens_mark + 1) {
                full_expression += "((";
            }
            full_expression += chain;

            if (index == actions_mark - 1) {
                full_expression += ")).#";
            } else {
                full_expression += ")|(";
            }

            index++;
        }
    }


    private boolean isExpressionSymbol(String symbol) {
        char character = symbol.charAt(0);
        int char_value = Integer.valueOf(character);
        return (char_value >= 40 && char_value <= 43) || char_value == 46 || char_value == 63 || char_value == 124;
    }


    private boolean searchSet(String chain) {
        for (String value : sets_names) {
            if (value.length() >= chain.length()) {
                if (chain.equals(value.substring(0, chain.length()))) {
                    if (chain.equals(value)) {
                        foundSet = true;
                    }
                    return true;
                }
            }
        }
        return false;
    }


    private void insertLeaf(String value) {
        Node temp = new Node();
        temp.value = value;
        temp.first.add(leafs.size() + 1);
        temp.last.add(leafs.size() + 1);

        leafs.put(leafs.size() + 1, value);
        stack.addFirst(temp);
    }


    private void calculateNewNode(String value) {
        Node temp = new Node();
        Node c1, c2;
        if (!stack.isEmpty()) {
            switch (value) {
                case ".":
                    c2 = stack.removeFirst();
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + c2.value + value);
                    if (c1.nullable && c2.nullable) {
                        temp.nullable = true;
                    }
                    if (c1.nullable) {
                        temp.first.addAll(c1.first);
                        temp.first = addNoRepsInList(temp.first, c2.first);
                    } else {
                        temp.first = c1.first;
                    }
                    if (c2.nullable) {
                        temp.last.addAll(c2.last);
                        temp.last = addNoRepsInList(temp.last, c1.last);
                    } else {
                        temp.last = c2.last;
                    }
                    calculateFollow(c1.last, c2.first);

                    break;
                case "|":
                    c2 = stack.removeFirst();
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + c2.value + value);
                    if (c1.nullable || c2.nullable) {
                        temp.nullable = true;
                    }
                    temp.first.addAll(c1.first);
                    temp.first = addNoRepsInList(temp.first, c2.first);
                    temp.last.addAll(c2.last);
                    temp.last = addNoRepsInList(temp.last, c1.last);
                    break;
                case "*":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    temp.nullable = true;
                    temp.first = c1.first;
                    temp.last = c1.last;
                    calculateFollow(c1.last, c1.first);
                    break;
                case "?":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    temp.nullable = true;
                    temp.first = c1.first;
                    temp.last = c1.last;
                    calculateFollow(c1.last, c1.first);
                    break;
                case "+":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    if (c1.nullable) {
                        temp.nullable = true;
                    }
                    temp.first = c1.first;
                    temp.last = c1.last;
                    calculateFollow(c1.last, c1.first);
                    break;
                default:
                    break;
            }
            stack.addFirst(temp);
        }
    }

    private ArrayList<Integer> addNoRepsInList(ArrayList<Integer> list, ArrayList<Integer> secondary_list) {
        for (Integer num : secondary_list) {
            if (!list.contains(num)) {
                list.add(num);
            }
        }
        return list;
    }

    private void calculateFollow(ArrayList<Integer> lasts, ArrayList<Integer> firsts) {
        for (Integer value : lasts) {
            if (follows.containsKey(value)) {
                addNoRepsInList(follows.get(value), firsts);
            } else {
                follows.put(value, firsts);
            }
        }
    }

    private boolean existInList(ArrayList<State> list, ArrayList<Integer> value) {
        for (State state : list) {
            if (state.getValue().size() == value.size()) {
                if (state.getValue().containsAll(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int searchInList(ArrayList<State> list, ArrayList<Integer> value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getValue().size() == value.size()) {
                if (list.get(i).getValue().containsAll(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public String getDFA() {
        String print = "A = (E,Q,f,q0,F)\n";
        print += "E:\n";
        ArrayList<String> chars = new ArrayList();
        for (int i = 1; i < leafs.size(); i++) {
            if (!chars.contains(leafs.get(i))) {
                chars.add(leafs.get(i));
            }
        }
        for (int i = 0; i < chars.size(); i++) {
            print += chars.get(i) + "\n";
        }
        print += "\nQ:\n";
        for (int i = 0; i < states.size(); i++) {
            print += states.get(i).state_name + "\n";
        }
        print += "\nf:\n";
        for (int i = 0; i < transitions.size(); i++) {
            print += transitions.get(i).getString() + "\n";
        }
        print += "\nF:\n";
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).final_state) {
                print += states.get(i).state_name + "\n";
            }
        }

        return print;
    }
    
    private void addText(String new_text){
        print_text += new_text + "\n";
    }
    
    private void initializeNewClass(){
        print_text = "Package Classes";
    }
}
