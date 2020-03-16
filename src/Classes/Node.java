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
public class Node {
     public String value;
    public boolean nullable = false;
    public ArrayList<Integer> first = new ArrayList<>();
    public ArrayList<Integer> last = new ArrayList<>();
}
