/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

/**
 *
 * @author Edgar
 */
public class Transition {
    String initial_state_name, final_state_name;
    String move;
    
    public Transition() {

    }

    public String getString() {
        return "(" + initial_state_name + ", " + move + ") = " + final_state_name;
    }
}
