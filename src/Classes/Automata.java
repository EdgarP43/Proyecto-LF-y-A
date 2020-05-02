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
import java.util.Map;
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
    HashMap<String, Integer> simple_tokens = new HashMap();
    HashMap<String, Integer> complex_tokens = new HashMap();
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
        text = file.readAutomatonFile(file_name);

        if (file.errors.isEmpty()) {
            sets_mark = file.getSets_mark();
            tokens_mark = file.getTokens_mark();
            actions_mark = file.getActions_mark();

            saveSets();
            saveTokens();
            saveActions();
            createDFA();

            initializeNewClass();
            buildValidationMethod();
            endNewClass();
            file.writeFile("Automata", print_text, "", "C:\\Users\\Edgar\\Desktop\\New folder\\", "cs");

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

    /**
     * Method to save the symbols from every set in the file
     */
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
                            //Normal assign
                            elements.add(Character.toString((char) num));
                            if ((point_counter % 2 == 0) && point_counter > 0) {
                                //Range assign
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

                    //Quotes and points count
                    if (s_character.equals("'")) {
                        quote_counter++;
                    } else if (s_character.equals(".")) {
                        point_counter++;
                    }

                    //Normal assign
                    if (!((quote_counter % 2) == 0)) {
                        if (!s_character.equals("'")) {
                            actual += s_character;
                        }
                        if (point_counter >= 0 && s_character.equals(".")) {
                            point_counter--;
                        }
                    } else {
                        //Validates that actual is not empty
                        if (!actual.equals("")) {
                            //Normal assign
                            elements.add(actual);
                            if ((point_counter % 2 == 0) && point_counter > 0) {
                                //Range assign
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

    private void saveTokens() {
        int index = tokens_mark + 1;
        while (index < actions_mark) {
            if (!text[index].equals("")) {
                int identifier = 0;
                for (int i = 0; i < text[index].length(); i++) {
                    if (text[index].charAt(i) == '=') {
                        identifier = i;
                        break;
                    }
                }

                String expression = text[index].substring(identifier + 1);
                int num = Integer.valueOf(text[index].substring(5, identifier));

                if (containsExpressionSymbol(expression)) {
                    complex_tokens.put(expression, num);
                } else {
                    String aux = "";
                    for (int i = 0; i < expression.length(); i++) {
                        if (expression.charAt(i) != '\'') {
                            aux += expression.charAt(i);
                        }
                    }
                    simple_tokens.put(aux, num);
                }
            }

            index++;
        }
    }

    private boolean containsExpressionSymbol(String expression) {
        int position = 0;
        int count_quotes = 0;
        boolean hasQuotes = false;
        if (expression.contains("'")) {
            hasQuotes = true;
        }
        while (position < expression.length()) {
            char character = expression.charAt(position);
            String s_character = String.valueOf(character);
            int char_value = Integer.valueOf(character);

            if (char_value == 39) {
                count_quotes++;
            }

            if (hasQuotes) {
                if (count_quotes % 2 == 0) {
                    if (isExpressionSymbol(s_character)) {
                        return true;
                    }
                }
            } else {
                if (isExpressionSymbol(s_character)) {
                    return true;
                }
            }

            position++;
        }
        return false;
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
                    } else {
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

    /**
     * Method to make a list of elements from the range that is given using
     * ascii return an array with all the selected characters
     *
     * @param begin
     * @param end
     * @param rangeList
     * @return
     */
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
        //String postfixExpression = "DIGITODIGITO*.'\"'CHARSET.'\"'.'''.CHARSET.'''||'='|'<''>'.|'<'|'>'|'>''='.|'<''='.|'+'|'-'|'O''R'.|'*'|'A''N'.'D'.|'M''O'.'D'.|'D''I'.'V'.|'N''O'.'T'.|'(''*'.|'*'')'.|';'|'.'|'{'|'}'|'('|')'|'['|']'|'.''.'.|':'|','|':''='.|LETRALETRADIGITO|*.|#.";
        //postfixExpression += "#.";
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
                    //insert a new leaf in the stack
                    insertLeaf(aux);
                    aux = "";
                    chain = "";
                    no_set = false;
                } else {
                    if (!searchSet(chain)) {
                        if (isExpressionSymbol(s_character)) {
                            calculateNewNode(s_character);
                        } else {
                            //insert a new leaf in the stack
                            insertLeaf(chain);
                        }
                        chain = "";
                    } else {
                        if (foundSet) {
                            //Insert a leaf with the name of the set
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

    /**
     * Method to get all the transitions from the Automata, starting from the root
 which is the first from the the full expression, only element in the
 stack
     */
    private void makeTransitions() {
        int state_count = 0;
        ArrayList<State> new_states = new ArrayList<>();
        boolean finish = false;
        State init_state = new State("q" + state_count);
        init_state.init_state = true;
        init_state.setValue(stack.peek().first);

        do {
            states.add(init_state);
            /*ArrayList to save all the follows from the existing transitions
            for the state*/
            ArrayList<State> temp_states = new ArrayList<>();
            ArrayList<Integer> follow_list;
            State temp;
            for (Integer num : init_state.getValue()) {
                temp = new State();
                //Gets the string or char to make a transition
                String move_chars = leafs.get(num);
                //Gets the list of the follows from the leaf number
                follow_list = new ArrayList<>();
                follow_list = follows.get(num);

                //Temporary state will have as a name the chain of chars obteined 
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

            /*After getting all the possible transitions it safe the new 
            states in the principal list*/
            for (int i = 0; i < temp_states.size(); i++) {

                Transition aux_transition = new Transition();
                /*First it gets the chain used to make the transition which
                is the actual name of temp*/
                aux_transition.move = temp_states.get(i).state_name;
                //The initial_state of the move it's the actual init_state
                aux_transition.initial_state_name = init_state.state_name;
                //The final state of the transition it's the new name of temp
                /*First, increments in one the state count to rename the state
                if there's a new state doesn't exist*/
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
                //Validate if 

                transitions.add(aux_transition);

                //Save a new state
                if (!existInList(states, temp_states.get(i).getValue())) {

                    if (!existInList(new_states, temp_states.get(i).getValue())) {
                        temp = temp_states.get(i);
                        temp.state_name = aux_transition.final_state_name;
                        //Adds the new state 
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

    /**
     * Method to turn all the tokens into a one and only expression
     */
    private void getFullExpression() {
        int index = tokens_mark + 1;
        while (index < actions_mark) {
            String chain = "";
            if (!text[index].equals("")) {

                if (text[index - 1].equals("")) {
                    full_expression += ")|(";
                }

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

                if ((index + 1) < actions_mark) {
                    if (!text[index + 1].equals("")) {
                        full_expression += ")|(";
                    }
                }

            }

            index++;
        }
        full_expression += ")).#";
        full_expression.replaceAll("|()", "");
    }

    /**
     * Method to validate if a character is part of the symbolism used into the
     * regular expression to make an operation
     *
     * @param symbol
     * @return
     */
    private boolean isExpressionSymbol(String symbol) {
        char character = symbol.charAt(0);
        int char_value = Integer.valueOf(character);
        return (char_value >= 40 && char_value <= 43) || char_value == 46 || char_value == 63 || char_value == 124;
    }

    /**
     * Method that looks for some similarities in the names of the sets until it
     * finds an specific name
     *
     * @param chain
     * @return
     */
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

    /**
     * Method to create a leaf node with an specific value
     *
     * @param value
     */
    private void insertLeaf(String value) {
        Node temp = new Node();
        temp.value = value;
        temp.first.add(leafs.size() + 1);
        temp.last.add(leafs.size() + 1);

        leafs.put(leafs.size() + 1, value);
        stack.addFirst(temp);
    }

    /**
     * Method to create a new node from an operation symbol and calculates all
     * the properties of the node as from the different operators
     *
     * @param value
     */
    private void calculateNewNode(String value) {
        Node temp = new Node();
        Node c1, c2;
        if (!stack.isEmpty()) {
            switch (value) {
                case ".":
                    c2 = stack.removeFirst();
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + c2.value + value);
                    //Nullability
                    if (c1.nullable && c2.nullable) {
                        temp.nullable = true;
                    }
                    //First
                    if (c1.nullable) {
                        temp.first.addAll(c1.first);
                        temp.first = addNoRepsInList(temp.first, c2.first);
                    } else {
                        temp.first = c1.first;
                    }
                    //Last
                    if (c2.nullable) {
                        temp.last.addAll(c2.last);
                        temp.last = addNoRepsInList(temp.last, c1.last);
                    } else {
                        temp.last = c2.last;
                    }
                    //Follow
                    calculateFollow(c1.last, c2.first);

                    break;
                case "|":
                    c2 = stack.removeFirst();
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + c2.value + value);
                    //Nullability
                    if (c1.nullable || c2.nullable) {
                        temp.nullable = true;
                    }
                    //First
                    temp.first.addAll(c1.first);
                    temp.first = addNoRepsInList(temp.first, c2.first);
                    //Last
                    temp.last.addAll(c2.last);
                    temp.last = addNoRepsInList(temp.last, c1.last);
                    break;
                case "*":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    //Nullability
                    temp.nullable = true;
                    //First
                    temp.first = c1.first;
                    //Last
                    temp.last = c1.last;
                    //Follow
                    calculateFollow(c1.last, c1.first);
                    break;
                case "?":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    //Nullability
                    temp.nullable = true;
                    //First
                    temp.first = c1.first;
                    //Last
                    temp.last = c1.last;
                    //Follow
                    calculateFollow(c1.last, c1.first);
                    break;
                case "+":
                    c1 = stack.removeFirst();

                    temp.value = (c1.value + value);
                    //Nullability
                    if (c1.nullable) {
                        temp.nullable = true;
                    }
                    //First
                    temp.first = c1.first;
                    //Last
                    temp.last = c1.last;
                    //Follow
                    calculateFollow(c1.last, c1.first);
                    break;
                default:
                    break;
            }
            //After calculating the properties from the new node, it gets added in the stack
            stack.addFirst(temp);
        }
    }

    /**
     * Method to add all the elements from the secondary list that are not
     * already in the primary list
     *
     * @param list
     * @param secundary_list
     * @return
     */
    private ArrayList<Integer> addNoRepsInList(ArrayList<Integer> list, ArrayList<Integer> secondary_list) {
        for (Integer num : secondary_list) {
            if (!list.contains(num)) {
                list.add(num);
            }
        }
        return list;
    }

    /**
     * Method to add the elements from the last to all the members from the
     * first list
     *
     * @param firsts
     * @param lasts
     */
    private void calculateFollow(ArrayList<Integer> lasts, ArrayList<Integer> firsts) {
        for (Integer value : lasts) {
            if (follows.containsKey(value)) {
                addNoRepsInList(follows.get(value), firsts);
            } else {
                follows.put(value, firsts);
            }
        }
    }

    /**
     * Method to search into a State list if it exist an specific value which is
     * a follow list
     *
     * @param list
     * @param value
     * @return
     */
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

    /**
     * Method to search into a State list if it exist an specific value which is
     * a follow list and return the index of the element in the list
     *
     * @param list
     * @param value
     * @return
     */
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

    private void initializeNewClass() {
        print_text = "using System; \nusing System.Collections.Generic;\nusing System.IO;\n";
        print_text += "using System.Linq;\n";
        print_text += "using System.Text;\nusing System.Threading.Tasks;\n";
        print_text += "namespace AutomatonValidator\n";
        print_text += "{\n";
        print_text += "public class AutomatonValidator{\n";
        print_text += "Dictionary<string, int> actions = new Dictionary<string, int>();\n";
        print_text += "Dictionary<string, int> simple_tokens = new Dictionary<string, int>();\n";
        print_text += "Dictionary<string, int> complex_tokens = new Dictionary<string, int>();\n";
        print_text += "List<string> final_states = new List<string>();\n\n";
        print_text += "public AutomatonValidator(){\n";
        print_text += "getActions();\n";
        print_text += "getSimpleTokens();\n";
        print_text += "getComplexTokens();\n";
        print_text += "getFinalStates();\n";
        print_text += "}\n\n";

        print_text += "private void getActions(){\n";
        for (Map.Entry<String, Integer> entry : actions.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("actions.Add(").append('"').append(entry.getKey()).append('"').append("," + entry.getValue()).append(");\n");
            print_text += builder.toString();
        }
        print_text += "}\n\n";

        print_text += "private void getSimpleTokens(){\n";
        for (Map.Entry<String, Integer> entry : simple_tokens.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("simple_tokens.Add(").append('"').append(entry.getKey()).append('"').append("," + entry.getValue()).append(");\n");
            print_text += builder.toString();
        }
        print_text += "}\n\n";

        print_text += "private void getComplexTokens(){\n";
        for (Map.Entry<String, Integer> entry : complex_tokens.entrySet()) {
            StringBuilder builder = new StringBuilder();
            String key = "";
            for (int i = 0; i < entry.getKey().length(); i++) {
                if (entry.getKey().charAt(i) == '"') {
                    key += "\\";
                }
                key += entry.getKey().charAt(i);
            }
            builder.append("complex_tokens.Add(").append('"').append(key).append('"').append("," + entry.getValue()).append(");\n");
            print_text += builder.toString();
        }
        print_text += "}\n\n";

        print_text += "private void getFinalStates(){\n";
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).final_state) {
                StringBuilder builder = new StringBuilder();
                builder.append("final_states.Add(").append('"').append(states.get(i).state_name).append('"').append(");\n");
                print_text += builder.toString();
            }
        }
        print_text += "}\n\n";

    }

    public void pppp() {
        for (Map.Entry<String, Integer> entry : actions.entrySet()) {
            System.out.println("Clave: " + entry.getKey() + " Valor: " + entry.getValue());
        }
    }

    private void endNewClass() {
        print_text += "}";
    }

    private void buildValidationMethod() {
        StringBuilder builder = new StringBuilder();
        //Method begging
        builder.append("public string validateEntries(string text){\n");
        builder.append("string result =").append('"').append('"').append(";\n");
        builder.append("string actual;\n");
        builder.append("actual = ").append('"').append("q0").append('"').append(";\n");
        builder.append("int index = 0;\n");
        builder.append("String actual_chain = ").append('"').append('"').append(";\n");
        builder.append("while(index < text.Length){\n");
        builder.append("char character = text.ToCharArray()[index];\n");
        builder.append("int char_value = Convert.ToInt32(character);\n\n");
        builder.append("if (char_value == 32 || char_value == 9 || char_value == 10) {\n");
        builder.append("if (!(actual_chain == null)) {\n");
        builder.append("if (actions.ContainsKey(actual_chain)) {\n");
        builder.append("result += actual_chain + ").append('"').append(" = ").append('"');
        builder.append(" + actions[actual_chain] + ").append('"').append("\\n").append('"').append(";\n");
        builder.append("} else if (simple_tokens.ContainsKey(actual_chain)) {\n");
        builder.append("result += actual_chain + ").append('"').append(" = ").append('"');
        builder.append(" + simple_tokens[actual_chain] + ").append('"').append("\\n").append('"').append(";\n");
        builder.append("} else {\n");
        builder.append("if(final_states.Contains(actual)){\n");
        builder.append("char aux = actual.ToCharArray()[0];\n");
        builder.append("int aux_char_value = Convert.ToInt32(aux);\n");
        int new_count = 0;
        int temp = 0;
        for (Map.Entry<String, Integer> entry : complex_tokens.entrySet()) {
            String start = getStartOfExpression(entry.getKey());
            if (new_count == 0) {
                temp = entry.getValue();
                builder.append("if( ");
            } else {
                builder.append("else if( ");
            }

            if (sets_names.contains(start)) {
                builder.append(getSetCondition2(start));
            } else {
                builder.append("aux == '" + start.charAt(0) + "'");
            }
            builder.append("){\n");
            builder.append("result += actual_chain + ").append('"').append(" = ").append('"').append(" + " + entry.getValue() + " + ")
                    .append('"').append("\\n").append('"').append(";\n");
            builder.append("}\n");
            new_count++;
        }
        if (complex_tokens.size() > 0) {
            builder.append("else{\n");
            builder.append("result += actual_chain + ").append('"').append(" = ").append('"').append(" + " + temp + " + ")
                    .append('"').append("\\n").append('"').append(";\n");
            builder.append("}\n");
        }

        builder.append("}else{\n");
        builder.append("result += actual_chain + ").append('"').append(" = E\\n").append('"').append(";\n");
        builder.append("}\n}\n");

        builder.append("actual_chain = ").append('"').append('"').append(";\n");
        builder.append("}\n");
        builder.append("actual = ").append('"').append("q0").append('"').append(";\n");
        builder.append("}else{\n");
        builder.append("switch(actual){\n");
        for (State temp_state : states) {
            builder.append("case ");
            builder.append('"');
            builder.append(temp_state.state_name);
            builder.append('"');
            builder.append(":\n");
            int cont = 0;
            for (Transition transition : transitions) {
                if (temp_state.state_name.equals(transition.initial_state_name)) {

                    if (cont == 0) {
                        builder.append("if(");
                    } else {
                        builder.append("else if(");
                    }

                    //Validate if the move it's a set to put a value on the condition
                    String condition = "";
                    if (sets_names.contains(transition.move)) {
                        condition = getSetCondition(transition.move);
                    } else {
                        //Validate if the move its a quote to add the slash \
                        if (transition.move.equals("'")) {
                            //Trabajar
                            builder.append("character == '").append('\\').append('\'').append("'){\n");
                        } else {
                            condition = "character == '" + transition.move + "'";
                        }
                    }

                    if (!condition.equals("")) {
                        builder.append(condition).append("){\n");
                    }

                    builder.append("actual = ").append('"').append(transition.final_state_name).append('"').append(";\n}\n");
                    cont++;
                }
            }
            if (cont != 0) {
                builder.append("else{\n");
                builder.append("actual = ").append('"').append("q").append('"').append(";}\n");
            }

            builder.append("break;\n");
        }
        builder.append("default:\nactual = ").append('"').append("q").append('"').append(";\nbreak;\n}\n");
        builder.append("actual_chain += character;\n}\n");
        builder.append("index++;\n");
        builder.append("}\n");  //End of the while condition
        builder.append("return result;\n");
        builder.append("}\n");
        builder.append("}\n");
        builder.append("class Program\n");
        builder.append("{\n");
        builder.append("static void Main(string[] args)\n");
        builder.append("{\n");
        builder.append("Console.WriteLine(\"Ingrese la direccion del archivo\");\n");
        builder.append("string direccion = null;\n");
        builder.append("direccion = Console.ReadLine();\n");
        builder.append(" using (StreamReader lector = new StreamReader(direccion))\n");
        builder.append("{\n");
        builder.append("string line;\n");
        builder.append("while ((line = lector.ReadLine()) != null)\n");
        builder.append("{\n");
        builder.append("AutomatonValidator automata = new AutomatonValidator();\n");
        builder.append("string prueba = automata.validateEntries(line);\n");
        builder.append("Console.WriteLine(prueba); \n");
        builder.append("}\n");
        builder.append("}\n");
        builder.append("Console.ReadKey();\n");
        builder.append("}\n");
        builder.append("}\n");
        

        print_text += builder.toString();
    }

    private String getSetCondition(String set_name) {
        ArrayList<String> values = new ArrayList<>();
        values = sets.get(set_name);
        String condition = "";
        int cont = 0;
        if (set_name.equals("CHARSET") || set_name.equals("charset")) {
            int menor = 256;
            int mayor = 0;
            for (int i = 0; i < 10; i++) {
                int num_value = Integer.valueOf(values.get(i).charAt(0));
                if (num_value < menor) {
                    menor = num_value;
                }
                if (num_value > mayor) {
                    mayor = num_value;
                }
            }
            condition = "char_value >= " + menor + " && char_value <= " + mayor;
        } else {
            for (int i = 0; i < values.size(); i++) {
                if (i != values.size() - 1) {
                    condition += "character == '" + values.get(i) + "' || ";
                } else {
                    condition += "character == '" + values.get(i) + "'";
                }
                cont++;
                if (cont == 6) {
                    condition += "\n";
                    cont = 0;
                }
            }
        }

        return condition;
    }

    private String getSetCondition2(String set_name) {
        ArrayList<String> values = new ArrayList<>();
        values = sets.get(set_name);
        String condition = "";
        int cont = 0;
        if (set_name.equals("CHARSET") || set_name.equals("charset")) {
            int menor = 256;
            int mayor = 0;
            for (int i = 0; i < 10; i++) {
                int num_value = Integer.valueOf(values.get(i).charAt(0));
                if (num_value < menor) {
                    menor = num_value;
                }
                if (num_value > mayor) {
                    mayor = num_value;
                }
            }
            condition = "aux_char_value >= " + menor + " && aux_char_value <= " + mayor;
        } else {
            for (int i = 0; i < values.size(); i++) {
                if (i != values.size() - 1) {
                    condition += "aux == '" + values.get(i) + "' || ";
                } else {
                    condition += "aux == '" + values.get(i) + "'";
                }
                cont++;
                if (cont == 6) {
                    condition += "\n";
                    cont = 0;
                }
            }
        }

        return condition;
    }

    private String getStartOfExpression(String expression) {
        String result = "";
        boolean searchSet = true;
        if (expression.charAt(0) == '\'') {
            searchSet = false;
        }
        for (int i = 0; i < expression.length(); i++) {
            if (searchSet) {
                result += expression.charAt(i);
                if (sets_names.contains(result)) {
                    break;
                }
            } else {
                if (expression.charAt(i) == '\'' && i != 0) {
                    break;
                }
                if (expression.charAt(i) != '\'') {
                    result += expression.charAt(i);
                }
            }
        }
        return result;
    }

}
