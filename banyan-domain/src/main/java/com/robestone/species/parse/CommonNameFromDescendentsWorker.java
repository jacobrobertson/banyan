package com.robestone.species.parse;

/**
 * For a given species without a common name, will create one like
 * "Includes X, Y" in order to help you understand the tree at a higher level.
 * A great example is the Entognatha tree.  It has Two-pronged bristletails and Springtails
 * Another great example is Mustelinae because it includes badgers, weasels and polecats with qualifiers.
		African Striped Weasel
		European Badgers
		Ferret-badger
		Hog Badger
		Honey Badger
		Marbled Polecat
		Patagonian Weasel
		Striped Polecat
		Weasels
 * Also search for a max common-name length??  Maybe doesn't matter.
 * But there should be some sort of balance between number of names and depth.
 * Like, if I already have 2 names, why would I keep digging 5 levels deep.
 * 
 * Probably just look for the first two names, and go infinitely deep.
 * Take one level at a time, and at each level collect all names found so far,
 * and select the top two if there are two.
 * If I have a tie, then choose the shortest name.
 * Before calculating, select the common name portion.
 * Should take into account the number of descendents in each sub-tree to settle ties.  
 * 	For example, look at all immediate children, and if there are 100 descendents in one, check it out.
 * 
 * @author jacob
 */
public class CommonNameFromDescendentsWorker extends AbstractWorker {

}
