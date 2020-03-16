/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.util.ArrayList;
/**
 *
 * @author Edgar
 */
public class State {
    String state_name;
    ArrayList<Integer> value;
    boolean init_state, final_state;

    public State(String name) {
        state_name = name;
    }

    public State() {

    }

    public ArrayList<Integer> getValue() {
        return value;
    }

    public void setValue(ArrayList<Integer> value) {
        this.value = value;
    }

    public String getString() {
        if(init_state && final_state){
            return "# -> " + state_name;
        } else if (init_state) {
            return "-> " + state_name;
        } else if (final_state) {
            return "# " + state_name;
        } else {
            return "  " + state_name;
        }
    }
}
