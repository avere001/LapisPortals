 package me.fetusdip.LapisPortals;
 
 public class ValidPortalReturn
 {
   private int chestHash;
   private boolean isValid;
   private int face;
   
   ValidPortalReturn()
   {
     this.chestHash = 0;
     this.isValid = false;
     this.face = -1;
   }
   
   public void setValid()
   {
     this.isValid = true;
   }
   
   public void setHash(int newChestHash)
   {
     this.chestHash = newChestHash;
   }
   
   public void setFace(int newFace)
   {
     this.face = newFace;
   }
   
   public boolean isValid()
   {
     return this.isValid;
   }
   
   public int getFace()
   {
     return this.face;
   }
   
   public int getHash()
   {
     return this.chestHash;
   }
 }