/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Edgar
 */
public class FileManager {

    boolean foundSet = false;
    boolean lookingForSet = false;
    int sets_mark = 0, tokens_mark, actions_mark;
    String[] split = null;
    ArrayList<String> sets = new ArrayList();
    ArrayList<String> tokens_nums = new ArrayList();
    ArrayList<String> actions_nums = new ArrayList();
    public ArrayList<String> errors = new ArrayList();

    public int getSets_mark() {
        return sets_mark;
    }

    public int getTokens_mark() {
        return tokens_mark;
    }

    public int getActions_mark() {
        return actions_mark;
    }

    /**
     * Method to read a file line by line and keep every line in different
     * positions into an array
     *
     * @param name name of the file
     * @return array of the lines
     */
    public String[] readAutomatonFile(String name) {
        File file = new File("C:\\Users\\Edgar\\Desktop\\New folder\\"+name+".txt");

        int count = 0;
        try {
            if (file.exists()) {
                FileReader readerfile, readerfile2;
                try {
                    readerfile = new FileReader(file);
                    readerfile2 = new FileReader(file);
                    BufferedReader reader = new BufferedReader(readerfile);
                    BufferedReader reader2 = new BufferedReader(readerfile2);
                    String line = "";
                    String temp = "";
                    try {
                        temp = reader.readLine();
                        while (temp != null) {
                            count++;
                            temp = reader.readLine();
                        }

                        split = new String[count];
                        count = 0;
                        line = reader2.readLine();
                        while (line != null) {

                            split[count] = line;
                            line = reader2.readLine();
                            count++;
                        }

                        readerfile.close();
                        reader.close();
                        readerfile2.close();
                        reader2.close();

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else {
                //Error de que no existe el archivo
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        replaceSpaces();
        makeValidations();
        return split;
    }

    public boolean writeFile(String file_name, String content, String error, String root, String ext) {
        File file = new File(root + file_name + "." + ext);
        if (file.exists()) {
            deleteFile(root + file_name + "." + ext);
        }

        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();

            return true;
        } catch (IOException ex) {
            error = ex.getMessage();
            return false;
        }
    }

    private boolean deleteFile(String file_name) {
        File file = new File(file_name);
        try {
            file.delete();
            file.createNewFile();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method to replace every space that shows in the text file
     */
    private void replaceSpaces() {
        int index = 0;
        while (index < split.length) {
            int position = 0;
            String new_chain = "";
            while (position < split[index].length()) {
                int quote_counter = 0;

                char character = split[index].charAt(position);
                String s_character = String.valueOf(character);
                int char_value = Integer.valueOf(character);

                if (char_value == 9) {
                    new_chain += "";
                }//Validates that the char is a quote, raises the count
                else if (char_value == 39) {
                    quote_counter++;
                    new_chain += s_character;
                } else if (char_value == 32) {
                    //Validates if the count its a pair number to replace the 
                    if (quote_counter % 2 == 0) {
                        new_chain += "";
                    } else {
                        new_chain += s_character;
                    }
                } else {
                    new_chain += s_character;
                }

                position++;
            }
            split[index] = new_chain;

            if (new_chain.equals("TOKENS")) {
                tokens_mark = index;
            } else if (new_chain.equals("ACTIONS")) {
                actions_mark = index;
            }

            index++;
        }
    }

    /**
     * Method to call all the validations methods
     */
    private void makeValidations() {
        validateSets();
        validateTokens();
        validateActions();

    }

    /**
     * Method to validate that all the syntax in the sets section it's correct
     */
    private void validateSets() {

        int index = sets_mark + 1;
        while (index < tokens_mark) {
            boolean set_name = false;
            boolean fullname = false;
            int position = 0;
            int quote_counter = 0;
            int point_counter = 0;
            float opp_counter = 0;
            String error = "";
            String prev = "";
            String prev_value = "";
            String aux = "";
            String temp_set_name = "";
            while (position < split[index].length()) {
                if (!split[index].equals("")) {
                    char character = split[index].charAt(position);
                    String s_character = String.valueOf(character);
                    int char_value = Integer.valueOf(character);

                    if (!fullname) {
                        if (position == 0) {
                            //Validate that the name starts with a letter
                            if (!(char_value >= 65 && char_value <= 90)
                                    && !(char_value >= 97 && char_value <= 122)) {
                                error = "ERROR = line: " + (index + 1) + ", the set name has to start with a letter";
                                break;
                            } else {
                                temp_set_name += s_character;
                                set_name = true;
                            }
                        } else {

                            //Finds the '=' symbol to know the name is complete  
                            if (char_value == 61) {
                                fullname = true;

                                if (!set_name) {
                                    error = "ERROR = line: " + (index + 1) + ", the set doesn't has an identifier";
                                    break;
                                } else {
                                    //Validate if the set name already exists
                                    if (sets.contains(temp_set_name)) {
                                        error = "ERROR = line: " + (index + 1) + ", the set name already exists";
                                        break;
                                    } else {
                                        sets.add(temp_set_name);
                                    }
                                }
                                //The set name only can have letters and numbers
                            } else if (!(char_value >= 65 && char_value <= 90)
                                    && !(char_value >= 97 && char_value <= 122)
                                    && !(char_value >= 48 && char_value <= 57)) {
                                error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the set name can't contain a special char";
                                break;

                            } else {
                                temp_set_name += s_character;
                            }
                        }
                    } else {
                        //Quotes and points count
                        if (s_character.equals("'")) {
                            quote_counter++;
                        } else if (s_character.equals(".") && (quote_counter % 2 == 0)) {
                            point_counter++;
                        } else if (s_character.equals("+") && (quote_counter % 2 == 0)) {
                            opp_counter++;
                        }

                        if (quote_counter % 2 == 0) {
                            //Validate ranges
                            if (position < split[index].length() - 1) {
                                if (String.valueOf(split[index].charAt(position + 1)).equals("'")
                                        && prev.equals("'") && s_character.equals("'")) {
                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be three quotes in a row";
                                }
                            }

                            if ((point_counter % 2 == 0) && point_counter > 0) {
                                boolean flag = false;
                                if (!prev_value.equals(aux) && !prev_value.equals("")) {
                                    if (prev_value.length() == 1 && aux.length() == 1) {
                                        character = prev_value.charAt(0);
                                        int aux_value = Integer.valueOf(character);
                                        character = aux.charAt(0);
                                        if (!(aux_value < Integer.valueOf(character))) {
                                            error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the range it's not valid";
                                        }
                                        flag = true;
                                        point_counter = 0;
                                        opp_counter++;
                                    } else {
                                        error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the range values only can contain 1 char";
                                    }
                                }
                                if (flag) {
                                    prev_value = "";
                                } else {
                                    prev_value = aux;
                                }
                                aux = "";
                            }
                        } else {
                            if (!s_character.equals("'")) {
                                aux += s_character;
                            }
                        }
                    }

                    if (s_character.equals("'") && prev.equals("'")) {
                        if (position < split[index].length() - 1) {
                            if (String.valueOf(split[index].charAt(position + 1)).equals("'")) {
                                quote_counter--;
                            } else {
                                if ((quote_counter % 2 == 0)) {
                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be empty quotes";
                                    break;
                                }
                            }
                        } else {
                            if ((quote_counter % 2 == 0)) {
                                if (!String.valueOf(split[index].charAt(position - 2)).equals("'")) {
                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be empty quotes";
                                    break;
                                }
                            }
                        }
                    }

                    prev = s_character;
                    position++;
                    if (position == (split[index].length())) {
                        if (!(quote_counter % 2 == 0)) {
                            error = "ERROR = line: " + (index + 1) + ", there's missing a '";
                            break;
                        } else if (!(point_counter % 2 == 0)) {
                            error = "ERROR = line: " + (index + 1) + ", there's missing a .";
                            break;
                        } else if (((quote_counter - 2) / opp_counter) != 2.00 && quote_counter != 0) {
                            error = "ERROR = line: " + (index + 1) + ", there's missing a +";
                            break;
                        }
                    }
                }

            }

            if (!error.equals("")) {
                errors.add(error);
            }
            index++;
        }
    }

    /**
     * Method to validate that all the syntax in the token section it's correct
     */
    private void validateTokens() {
        String error = "";
        int index = tokens_mark + 1;
        while (index < actions_mark) {
            if (!split[index].equals("")) {
                //The token line has the identifier 'Token' as the first 5 positions
                if (split[index].substring(0, 5).toUpperCase().equals("TOKEN")) {
                    int position = 5;
                    boolean fullname = false;
                    boolean number = false;
                    boolean first = true;
                    String num = "";
                    String chain = "";
                    String prev = "";
                    int quote_counter = 0;

                    while (position < split[index].length()) {
                        if (!split[index].equals("")) {

                            char character = split[index].charAt(position);
                            String s_character = String.valueOf(character);
                            int char_value = Integer.valueOf(character);

                            //Validates if the identifier of the token is complete
                            if (!fullname) {
                                /*Validates the rest of the identifier, starting for the
                        token number*/
                                if ((char_value >= 48 && char_value <= 57)) {
                                    number = true;
                                    num += s_character;
                                    //Finds the '=' symbol to know the name is complete    
                                } else if (char_value == 61) {
                                    fullname = true;

                                    if (!number) {
                                        error = "ERROR = line: " + (index + 1) + ", the identifier doesn't have a number. Ex: TOKEN2";
                                        break;
                                    } else {
                                        if (tokens_nums.isEmpty()) {
                                            tokens_nums.add(num);
                                        } else if (!tokens_nums.contains(num)) {
                                            tokens_nums.add(num);
                                        } else {
                                            error = "ERROR = line: " + (index + 1) + ", the token number already exists";
                                            break;
                                        }
                                    }
                                } else {
                                    error = "ERROR = line: " + (index + 1) + ", column: : " + (position + 1) + ", There's missing a '=' to initialize the token";
                                    break;
                                }

                            } else {
                                if (s_character.equals("'")) {
                                    quote_counter++;
                                }
                                //Is the first character from the expression
                                if (first) {
                                    if ((char_value != 39 && char_value != 40) && isExpressionSymbol(s_character)) {
                                        error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the expression can't start with an operation symbol";
                                        break;
                                    } else {
                                        if (!s_character.equals("'") && !s_character.equals("(")) {
                                            chain = s_character;
                                        }
                                        if (!(char_value == 39 || char_value == 40)) {
                                            if (!searchSet(chain)) {
                                                error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the name of the set doesn't exists";
                                                break;
                                            } else {
                                                lookingForSet = true;
                                                chain = s_character;
                                            }
                                        }
                                    }
                                    first = false;
                                } else {
                                    if (lookingForSet) {
                                        chain += s_character;
                                        if (!searchSet(chain)) {
                                            error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the name of the set doesn't exists";
                                            lookingForSet = false;
                                            break;
                                        }
                                    } else {
                                        /*If it doesn't looks for a set it will work with the previous char:
                                Validates if both are proper symbols of the regular expression and won't send an
                                error only if the symbol is '('*/
                                        if (isExpressionSymbol2(s_character) && isExpressionSymbol2(prev) && (quote_counter % 2 == 0)) {
                                            if (!prev.equals("(") && !prev.equals(")")) {
                                                if (!s_character.equals("(") && !s_character.equals(")")) {
                                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be 2 operation symbols next to each other";
                                                    break;
                                                }

                                            } else {
                                                if (s_character.equals(")")) {
                                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be empty parenthesis";
                                                    break;
                                                }
                                            }
                                        } else {
                                            if (!isExpressionSymbol(s_character) && (quote_counter % 2 == 0)) {
                                                chain += s_character;
                                                if (!searchSet(chain)) {
                                                    error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", the name of the set doesn't exists";
                                                    lookingForSet = false;
                                                    break;
                                                } else {
                                                    lookingForSet = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (foundSet) {
                                chain = "";
                                lookingForSet = false;
                                foundSet = false;
                            }

                            if (s_character.equals("'") && prev.equals("'")) {
                                if (position < split[index].length() - 1) {
                                    if (String.valueOf(split[index].charAt(position + 1)).equals("'")) {
                                        quote_counter--;
                                        chain = "";
                                    }
                                } else {
                                    if ((quote_counter % 2 == 0)) {
                                        if (!String.valueOf(split[index].charAt(position - 2)).equals("'")) {
                                            error = "ERROR = line: " + (index + 1) + ", column: " + (position + 1) + ", it can't be empty quotes";
                                            break;
                                        }
                                    }
                                }
                            }

                            prev = s_character;
                            position++;
                            if (position == (split[index].length())) {
                                if (!(quote_counter % 2 == 0)) {
                                    error = "ERROR = line: " + (index + 1) + ", there's missing a '";
                                    break;
                                }
                            }
                        }

                    }
                } else {
                    error = "ERROR = line: " + (index + 1) + ", the identifier 'TOKEN' doesn't exists";
                }
            }

            if (!error.equals("")) {
                errors.add(error);
            }

            index++;
            error = "";
        }
    }

    private void validateActions() {
        String error = "";
        int index = actions_mark + 3;
        while (index < split.length) {
            if (split[index].equals("}")) {
                break;
            }

            int position = 0;
            boolean full_name = false;
            boolean identifier = false;
            boolean first = true;
            String chain = "";
            String prev = "";
            String num = "";
            int quote_counter = 0;

            if (!split[index].equals("")) {
                while (position < split[index].length()) {
                    char character = split[index].charAt(position);
                    String s_character = String.valueOf(character);
                    int char_value = Integer.valueOf(character);

                    if (!full_name) {
                        if ((char_value >= 48 && char_value <= 57)) {
                            num += s_character;
                            identifier = true;
                        } else if (s_character.equals("=")) {
                            if (identifier) {
                                if (!actions_nums.isEmpty()) {
                                    if (actions_nums.contains(num)) {
                                        error = "ERROR = line: " + (index + 1) + ", the number already exists";
                                        break;
                                    }
                                }
                                actions_nums.add(num);
                                full_name = true;
                            } else {
                                error = "ERROR = line: " + (index + 1) + ", there's missing a identifier";
                                break;
                            }
                        } else {
                            error = "ERROR = line: " + (index + 1) + ", there's missing a point to start the actions";
                            break;
                        }
                    } else {
                        if (s_character.equals("'")) {
                            quote_counter++;
                        }

                        if (first) {
                            if (quote_counter != 1) {
                                error = "ERROR = line: " + (index + 1) + ", the word should start with a quote";
                                break;
                            }
                            first = false;
                        } else {
                            if (s_character.equals("'")) {
                                if (chain.isEmpty() || chain.equals("")) {
                                    error = "ERROR = line: " + (index + 1) + ", there's must be a word inside the quotes";
                                    break;
                                }
                            } else {
                                chain += s_character;
                            }
                        }
                    }
                    prev = s_character;
                    position++;
                    if (position == split[index].length()) {
                        if (quote_counter % 2 != 0) {
                            error = "ERROR = line: " + (index + 1) + ", there's missing a quote";
                            break;
                        }
                    }
                }
            }

            if (!error.equals("")) {
                errors.add(error);
            }

            index++;
            error = "";
        }
    }

    private boolean isExpressionSymbol(String symbol) {
        char character = symbol.charAt(0);
        int char_value = Integer.valueOf(character);
        return (char_value >= 39 && char_value <= 43) || char_value == 63 || char_value == 124;
    }

    private boolean searchSet(String chain) {
        for (String value : sets) {
            if (chain.length() <= value.length()) {
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

    private boolean isExpressionSymbol2(String symbol) {
        char character = symbol.charAt(0);
        int char_value = Integer.valueOf(character);
        return (char_value >= 40 && char_value <= 43) || char_value == 46 || char_value == 63 || char_value == 124;
    }
}
