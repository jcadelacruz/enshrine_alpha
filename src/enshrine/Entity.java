/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enshrine;

import exceptions.ItemNotInInventoryException;
import exceptions.OutOfGameScreenBoundsException;
import exceptions.OutOfResourceCapacityBoundsException;
import exceptions.NotAffordableException;
import java.util.ArrayList;
import javafx.scene.image.ImageView;

/**
 * Entity represents the moving NPCs. Everything revolves around them since they're the only ones that can move and interact with anything (besides sacrificing which the player can do on their own)
 * 
 * @author dc_ca
 */
public class Entity {
    public final static String ENEMY="HANNAH", DISCIPLE="LIKE_AND_SUBSCRIBE";//, USER="ITS_MAAM_ACTUALLY";
    public final static int WOOD=0, IRON=1, FOOD=2;
    
    //type
    protected String type;//Type distinguishes allies from foes; allies can't damage each other
    //general stats
    protected double hp, maxHP, str, def, atkSpd;//They can attack, hence hp-maxHP str def atkSpd
        //movement
    protected double moveSpd;//They can move, hence pos and moveSpd
    private int pos, targetPos = Game.GAME_SIZE*5/6;//pos describes the BOTTOM LEFT corner of the hit box of the entity; pos=0 means left side of the screen
    private int width = 60;//CURRENTLY SET TO DEFAULT; width of the hit box
    private Building buildingAttemptingToReach = null;
    private boolean insideBuilding = false, goingRight = true;
    private ImageView displayCharacter = null;
        //
    protected double actionSpd;//They can do building actions, hence actionSpd
        protected int iq;//They can craft, hence iq
    //inventory
        //inv-items
    protected ArrayList<Item> inventory = new ArrayList<>();//Inventory contains all items possessed by the entity
    private Item itemAttemptingToCraft = null, equippedArmor = null, equippedWeapon = null;//stats are added upon equip and de-equip
        //resources
    protected int woodCnt, ironCnt, foodCnt, capacity;
    
    public Entity(String t, double h, double s, double d, double ms, double as, int i, int p){
        type = t;
        switch(type){
            case ENEMY:
                goingRight = true;
                break;
            case DISCIPLE:
                goingRight = false;
                break;
        }
        
        hp = h;
        maxHP = h;
        str = s;
        def = d;
        moveSpd = ms;
        actionSpd = as;
        iq = i;
        
        pos = p;
        
        woodCnt = 0;
        ironCnt = 0;
        foodCnt = 0;
    }
    
    //getters
    public String getType(){ return type;}
        //stats
    public double[] getStats(){
        double[] res = {hp, maxHP, str, def, moveSpd, atkSpd, actionSpd, (double)iq};
        return res;
    }
            //position
    public int getPos(){ return pos;}
    public boolean getInsideBuilding(){ return insideBuilding;}
    public boolean getGoingRight(){ return goingRight;}
                //target
    public int getTargetPos(){ return targetPos;}
    public Building getBuildingAttemptingToReach(){ return buildingAttemptingToReach;}
                //size
    public int getWidth(){ return width;}
        //display
    public ImageView getDisplayCharacter(){ return displayCharacter;}
    public String getImageFileName(){ return "/imgs/not.png";}//TEMPORARY/ might not be necessary
        //inventory
            //inv-items
    public Item getItemAttemptingToCraft(){ return itemAttemptingToCraft;}
    public Item getequippedArmor(){ return equippedArmor;}
    public Item getequippedWeapon(){ return equippedWeapon;}
            //resources
    public int[] getMaterialCnts(){
        int[] res = {woodCnt, ironCnt, foodCnt};
        return res;
    }
    public int getCapacity(){ return capacity;}
    
    //setters
        //stats>position
    public void setTargetPos(double i){//percentage
        if(i>1) i = 1;
        if(i<0) i = 0;
        targetPos = (int) i*Game.GAME_SIZE;
    }
    public void setGoingRight(boolean b){ goingRight = b;}
        //display
    public void setDisplayCharacter(ImageView iv){ displayCharacter = iv;}
        //inv-items
    public void setItemAttemptingToCraft(Item i){ itemAttemptingToCraft = i;}
    
    //methods
        //stats
    public void addStat(double[] d){
        this.addMaxHP(d[1]);
        this.addHP(d[0]);
        str = addUntilZero(str, d[2]);
        def = addUntilZero(def, d[3]);
        moveSpd = addUntilZero(moveSpd, d[4]);
        atkSpd = addUntilZero(atkSpd, d[5]);
        actionSpd = addUntilZero(actionSpd, d[6]);
        iq = (int) addUntilZero((double) iq, d[7]);
    }
    public void addHP(double h){
        double health = hp + h;
        if(health>maxHP) health = maxHP;
        if(health<0) health = 0;
        hp = health;
    }
    public void addMaxHP(double h){
        maxHP += h;
        addHP(0);
    }
    public double addUntilZero(double currVal, double add){
        double sum = currVal + add;
        if(sum<0){
            //throw new BelowZeroException();
            sum = 0;
        }
        return sum;
    }
        //position
    public void setTarget(Building b){
        buildingAttemptingToReach = b;
        targetPos = (int) (b.getPos()+b.getWidth());
    }
    public void move() throws OutOfGameScreenBoundsException{
        if(goingRight){
            if(pos+moveSpd>Game.GAME_SIZE) throw new OutOfGameScreenBoundsException();
            pos+=moveSpd;
        }
        else{
            if(pos-moveSpd<0) throw new OutOfGameScreenBoundsException();
            pos-=moveSpd;
        }
    }
    //public void move(int i){ pos+=i;}
        //resources
    public void attemptAddMaterials(int[] m) throws NotAffordableException, OutOfResourceCapacityBoundsException{
        int failAt = 0;
        try{
            for(int i = 0; i<3; i++){
                attemptAddMaterials(m[i], i);
                failAt++;
            }
        }
        catch(NotAffordableException e){
            for(int i = 0; i<=failAt; i++){
                attemptAddMaterials(-m[i], i);
            }
            throw new NotAffordableException();
        }
        catch(OutOfResourceCapacityBoundsException e){
            for(int i = 0; i<=failAt; i++){
                attemptAddMaterials(-m[i], i);
            }
            throw new OutOfResourceCapacityBoundsException();
        }
    }
    public void attemptAddMaterials(int count, int i) throws NotAffordableException, OutOfResourceCapacityBoundsException{
        //convert
        int index = i;//convert section was made when public indices of resources wasn't 0, 1, and 2
        //check for exceptions
        if(!canAfford(count, index)) throw new NotAffordableException();
        if(!withinCapacity(count, index)) throw new OutOfResourceCapacityBoundsException();
        //add
        switch(index){
            case Entity.WOOD:
                woodCnt+=count;
                break;
            case Entity.IRON:
                ironCnt+=count;
                break;
            case Entity.FOOD:
                foodCnt+=count;
                break;
            default:
                System.out.println("Error in Entity class, attemptAddMaterials(): lol what even u tryna do"+WOOD+IRON+FOOD);
        }
    }
    public boolean canAfford(int count, int index){
        boolean affordable = false;
        switch(index){
            case Entity.WOOD:
                if(-count<=woodCnt) affordable = true;
                break;
            case Entity.IRON:
                if(-count<=ironCnt) affordable = true;
                break;
            case Entity.FOOD:
                if(-count<=foodCnt) affordable = true;
                break;
            default:
                System.out.println("Error in Entity class, canAfford(): helpme"+WOOD+IRON+FOOD);
        }
        return affordable;
    }
    public boolean withinCapacity(int count, int index){//in the future, i might add specific capacities to specific resources
        boolean within = false;
        switch(index){
            case Entity.WOOD:
                if(count+woodCnt<capacity) within = true;
                break;
            case Entity.IRON:
                if(count+ironCnt<capacity) within = true;
                break;
            case Entity.FOOD:
                if(count+foodCnt<capacity) within = true;
                break;
            default:
                System.out.println("Error in Entity class, canAfford(): helpme"+WOOD+IRON+FOOD);
        }
        return within;
    }
        //inventory-items
    public void addItem(Item i){ inventory.add(i);}
    public void takeItem(Item item) throws ItemNotInInventoryException{
        if (inventory.contains(item)) inventory.remove(item);
        else throw new ItemNotInInventoryException();
    }
    
}
