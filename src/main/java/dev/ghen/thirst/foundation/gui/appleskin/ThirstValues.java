package dev.ghen.thirst.foundation.gui.appleskin;

public class ThirstValues {
   public final int thirst;
   public final float quenchedModifier;

   public ThirstValues(int thirst, float saturationModifier) {
      this.thirst = thirst;
      this.quenchedModifier = saturationModifier;
   }

   public float getQuenchedIncrement() {
      return (float)this.thirst * this.quenchedModifier * 2.0F;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ThirstValues)) {
         return false;
      } else {
         ThirstValues that = (ThirstValues)o;
         return this.thirst == that.thirst && Float.compare(that.quenchedModifier, this.quenchedModifier) == 0;
      }
   }

   public int hashCode() {
      int result = this.thirst;
      result = 31 * result + (this.quenchedModifier != 0.0F ? Float.floatToIntBits(this.quenchedModifier) : 0);
      return result;
   }
}
