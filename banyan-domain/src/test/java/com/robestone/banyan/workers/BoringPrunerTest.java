package com.robestone.banyan.workers;

import java.util.ArrayList;
import java.util.Collection;

import com.robestone.banyan.taxons.AnalyzableTreeNode;
import com.robestone.banyan.taxons.Image;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.taxons.TreeNodeUtilities;

import junit.framework.TestCase;

public class BoringPrunerTest extends TestCase {

	public void testAlligatorsAndCaimans() {
		BoringPruner pruner = new BoringPruner();
		
		TaxonNode ac = completeEntry("Alligators & Caimans", "Alligatoridae", "pic1", 1, null);

		TaxonNode a1 = completeEntry("Alligators", "Alligatorinae", "pic1", 2, 1);
		TaxonNode a2 = completeEntry("Alligators", "Alligator", "pic1", 3, 2);
		
		TaxonNode aa = completeEntry("American Alligator", "ally amy", "pic1", 4, 3);
		TaxonNode ca = completeEntry("Chinese Alligator", "ally sino", "pic2", 5, 3);
		
		TaxonNode c = completeEntry("Caimans", "caiman", "pic3", 6, 1);
		
		
		Collection<TaxonNode> entries = new ArrayList<>();
		entries.add(ac);
		entries.add(a1);
		entries.add(a2);
		entries.add(aa);
		entries.add(ca);
		entries.add(c);
		
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);

		assertSame(a1, a2.getParent());
		assertSame(ac, a1.getParent());
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertNull(a1.getParent());
		assertSame(ac, a2.getParent());
		assertEquals(5, pruner.getInteresting().size());
		assertEquals(1, pruner.getBoring().size());
	}
	public void testPeppers() {
		BoringPruner pruner = new BoringPruner();
		TaxonNode parent = completeEntry("Papa Pepper", "z", "pic1", 1, null);
		TaxonNode child1 = completeEntry("Pepper", "Pepito sonriso", null, 2, 1);
		TaxonNode child2 = completeEntry("Pepper", "Pepito fruncir", "pic2", 3, 1);
		
		Collection<TaxonNode> entries = new ArrayList<>();
		entries.add(parent);
		entries.add(child1);
		entries.add(child2);
		
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);

		assertTrue(parent.getChildren().contains(child1));
		assertSame(child1.getParent(), parent);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertFalse(parent.getChildren().contains(child1));
		assertNull(child1.getParent());
	}
	public void testMantaBirostris() {
		BoringPruner pruner = new BoringPruner();
		TaxonNode parent = completeEntry("Eagle rays", "z", "pic1", 1, null);
		TaxonNode child = completeEntry("MAnta Rays", "manta", null, 2, 1);
		TaxonNode gchild = completeEntry("Manta Rays", "Manta birostris", "pic2", 3, 2);
		
		Collection<TaxonNode> entries = new ArrayList<>();
		entries.add(parent);
		entries.add(child);
		entries.add(gchild);
		
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);

		assertEquals(gchild.getParentId(), child.getId());
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertEquals(gchild.getParentId(), parent.getId());
	}
	
	public void testMountainBeaver() {
		BoringPruner pruner = new BoringPruner();
		TaxonNode parent = completeEntry("Mountain Beaver", "Aplodontia", "thumb/3/3c/Aplodontia.jpg/180px-Aplodontia.jpg", 1, null);
		TaxonNode child = completeEntry("Mountain Beaver", "Aplodontia rufa", "3/3c/Aplodontia.jpg", 2, 1);
		
		Collection<TaxonNode> entries = new ArrayList<>();
		entries.add(parent);
		entries.add(child);
		
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertTrue(pruner.getBoring().contains(child));
	}
	public void testAngelSharks() {
		BoringPruner pruner = new BoringPruner();
		TaxonNode gparent = completeEntry("Funk Shark", "Foo 1", null, 0, null);
		TaxonNode parent = completeEntry("Angel Shark", "Foo 1", null, 1, 0);
		TaxonNode child = completeEntry("Angel Sharks", "Foo 2", null, 2, 1);
		TaxonNode gchild = completeEntry("Angel Shark", "Foo 3", null, 3, 2);
		
		Collection<TaxonNode> entries = new ArrayList<>();
		entries.add(gparent);
		entries.add(parent);
		entries.add(child);
		entries.add(gchild);
		
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertTrue(pruner.getBoring().contains(child));
		assertTrue(pruner.getBoring().contains(gchild));
	}
	
	private void doAssertCollections(BoringPruner pruner) {
		for (AnalyzableTreeNode e: pruner.getInteresting()) {
			assertFalse(pruner.getBoring().contains(e));
		}
		for (AnalyzableTreeNode e: pruner.getBoring()) {
			assertFalse(pruner.getInteresting().contains(e));
		}
	}
	
	// TODO
//	Bromus hordeaceus
	
	private TaxonNode completeEntry(String cn, String ln, String ip, Integer id, Integer pid) {
		TaxonNode e = new TaxonNode();
		e.setCommonName(cn);
		e.setLatinName(ln);
		e.setTaxonId(id);
		e.setParentTaxonId(pid);
		
		Image i = new Image();
		i.setFilePath(ip);
		e.setImage(i);
		
		return e;
	}
	
}
