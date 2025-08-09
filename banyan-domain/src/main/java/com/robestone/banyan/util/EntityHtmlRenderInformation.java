/*
 * @version Apr 1, 2005
 */
package com.robestone.banyan.util;

import java.util.Arrays;

/**
 * Helper class that identifies entities that will have trouble rendering in html
 * 
 * @author Jacob Robertson
 */
public class EntityHtmlRenderInformation {

	public boolean isNameRequired(Entity e) {
		return isFound(e, useName);
	}
	public boolean isSearchTextRequired(Entity e) {
		if (!e.isNumberUsed()) {
			return true;
		} else {
			return isFound(e, useSearchText);
		}
	}
	private boolean isFound(Entity e, int[] a) {
		int s = e.getNumber();
		int pos = Arrays.binarySearch(a, s);
		return pos >= 0;
	}
	
	private static int[] useName = {
		714
	};
	private static int[] useSearchText = {
		482,
		483, 
		562,
		563, 
		596, 
		603, 
		628, 
		688, 
		695, 
		696, 
		697, 
		700, 
		715, 
		788, 
		977, 
		978, 
		982,
		2135,
		4800, 
		7692, 
		7693, 
		7694, 
		7695, 
		7716, 
		7717, 
		7722, 
		7723, 
		7730, 
		7731, 
		7734, 
		7735, 
		7742, 
		7743, 
		7744, 
		7745, 
		7746, 
		7747, 
		7748, 
		7749, 
		7750, 
		7751, 
		7770, 
		7771, 
		7774, 
		7775, 
		7778, 
		7779, 
		7788, 
		7789, 
		7790, 
		7791, 
		7826, 
		7827, 
		8048, 
		8501 
	};
	
	/*

These won't render any way possible

Aeligmac AE ? 482 ? 
aeligmac ae ? 483 ? 
Ymacr Y ? 562 ? 
ymacr y ? 563 ? 
irevc c ? 596 ? 
egr e ? 603 ? 
nsc N ? 628 ? 
hsmall h ? 688 ? 
wsmall w ? 695 ? 
ysmall y ? 696 ? 
primemodifier ' ? 697 ? 
glottalstop ' ? 700 ? 
grave &grave; ` ? 715 ? 
rough &rough; ' ? 788 ? 
thetasym ? ? ? 977 ? 
upsih ? Y ? 978 ? 
piv ? W ? 982 ? 
quesnodo &quesnodo; ? ? 4800 ? 
Dlowdot &Dlowdot; D ? 7692 ? 
dlowdot &dlowdot; d ? 7693 ? 
Dlowmacr &Dlowmacr; D ? 7694 ? 
dlowmacr &dlowmacr; d ? 7695 ? 
Hlowdot &Hlowdot; H ? 7716 ? 
hlowdot &hlowdot; h ? 7717 ? 
Hlowbrev &Hlowbrev; H ? 7722 ? 
hlowbrev &hlowbrev; h ? 7723 ? 
Klowdot &Klowdot; K ? 7730 ? 
klowdot &klowdot; k ? 7731 ? 
Llowdot &Llowdot; L ? 7734 ? 
llowdot &llowdot; l ? 7735 ? 
Macute &Macute; M ? 7742 ? 
macute &macute; m ? 7743 ? 
Mdot &Mdot; M ? 7744 ? 
mdot &mdot; m ? 7745 ? 
Mlowdot &Mlowdot; M ? 7746 ? 
mlowdot &mlowdot; m ? 7747 ? 
Ndot &Ndot; N ? 7748 ? 
ndot &ndot; n ? 7749 ? 
Nlowdot &Nlowdot; N ? 7750 ? 
nlowdot &nlowdot; n ? 7751 ? 
Rlowdot &Rlowdot; R ? 7770 ? 
rlowdot &rlowdot; r ? 7771 ? 
Rlowmacr &Rlowmacr; R ? 7774 ? 
rlowmacr &rlowmacr; r ? 7775 ? 
Slowdot &Slowdot; S ? 7778 ? 
slowdot &slowdot; s ? 7779 ? 
Tlowdot &Tlowdot; T ? 7788 ? 
tlowdot &tlowdot; t ? 7789 ? 
Tlowmacr &Tlowmacr; T ? 7790 ? 
tlowmacr &tlowmacr; t ? 7791 ? 
Zlowdot &Zlowdot; Z ? 7826 ? 
zlowdot &zlowdot; z ? 7827 ? 
agrgrave &agrgrave; a ? 8048 ? 
alefsym ? ? ? 8501 ? 

These need to render by the name
acute � ? ? 714 ? 


	*/
	
}
