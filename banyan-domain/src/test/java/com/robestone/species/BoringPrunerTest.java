package com.robestone.species;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

public class BoringPrunerTest extends TestCase {

	public void testAlligatorsAndCaimans() {
		BoringPruner pruner = new BoringPruner();
		
		CompleteEntry ac = completeEntry("Alligators & Caimans", "Alligatoridae", "pic1", 1, null);

		CompleteEntry a1 = completeEntry("Alligators", "Alligatorinae", "pic1", 2, 1);
		CompleteEntry a2 = completeEntry("Alligators", "Alligator", "pic1", 3, 2);
		
		CompleteEntry aa = completeEntry("American Alligator", "ally amy", "pic1", 4, 3);
		CompleteEntry ca = completeEntry("Chinese Alligator", "ally sino", "pic2", 5, 3);
		
		CompleteEntry c = completeEntry("Caimans", "caiman", "pic3", 6, 1);
		
		
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(ac);
		entries.add(a1);
		entries.add(a2);
		entries.add(aa);
		entries.add(ca);
		entries.add(c);
		
		Tree tree = EntryUtilities.buildTree(entries);

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
		CompleteEntry parent = completeEntry("Papa Pepper", "z", "pic1", 1, null);
		CompleteEntry child1 = completeEntry("Pepper", "Pepito sonriso", null, 2, 1);
		CompleteEntry child2 = completeEntry("Pepper", "Pepito fruncir", "pic2", 3, 1);
		
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(parent);
		entries.add(child1);
		entries.add(child2);
		
		Tree tree = EntryUtilities.buildTree(entries);

		assertTrue(parent.getChildren().contains(child1));
		assertSame(child1.getParent(), parent);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertFalse(parent.getChildren().contains(child1));
		assertNull(child1.getParent());
	}
	public void testMantaBirostris() {
		BoringPruner pruner = new BoringPruner();
		CompleteEntry parent = completeEntry("Eagle rays", "z", "pic1", 1, null);
		CompleteEntry child = completeEntry("MAnta Rays", "manta", null, 2, 1);
		CompleteEntry gchild = completeEntry("Manta Rays", "Manta birostris", "pic2", 3, 2);
		
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(parent);
		entries.add(child);
		entries.add(gchild);
		
		Tree tree = EntryUtilities.buildTree(entries);

		assertEquals(gchild.getParentId(), child.getId());
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertEquals(gchild.getParentId(), parent.getId());
	}
	
	public void testMountainBeaver() {
		BoringPruner pruner = new BoringPruner();
		CompleteEntry parent = completeEntry("Mountain Beaver", "Aplodontia", "thumb/3/3c/Aplodontia.jpg/180px-Aplodontia.jpg", 1, null);
		CompleteEntry child = completeEntry("Mountain Beaver", "Aplodontia rufa", "3/3c/Aplodontia.jpg", 2, 1);
		
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(parent);
		entries.add(child);
		
		Tree tree = EntryUtilities.buildTree(entries);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertTrue(pruner.getBoring().contains(child));
	}
	public void testAngelSharks() {
		BoringPruner pruner = new BoringPruner();
		CompleteEntry gparent = completeEntry("Funk Shark", "Foo 1", null, 0, null);
		CompleteEntry parent = completeEntry("Angel Shark", "Foo 1", null, 1, 0);
		CompleteEntry child = completeEntry("Angel Sharks", "Foo 2", null, 2, 1);
		CompleteEntry gchild = completeEntry("Angel Shark", "Foo 3", null, 3, 2);
		
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(gparent);
		entries.add(parent);
		entries.add(child);
		entries.add(gchild);
		
		Tree tree = EntryUtilities.buildTree(entries);
		
		pruner.prune(tree);
		doAssertCollections(pruner);
		
		assertTrue(pruner.getBoring().contains(child));
		assertTrue(pruner.getBoring().contains(gchild));
	}
	
	private void doAssertCollections(BoringPruner pruner) {
		for (CompleteEntry e: pruner.getInteresting()) {
			assertFalse(pruner.getBoring().contains(e));
		}
		for (CompleteEntry e: pruner.getBoring()) {
			assertFalse(pruner.getInteresting().contains(e));
		}
	}
	
	// TODO
//	Bromus hordeaceus
	
	private CompleteEntry completeEntry(String cn, String ln, String i, Integer id, Integer pid) {
		CompleteEntry e = new CompleteEntry();
		e.setCommonName(cn);
		e.setLatinName(ln);
		e.setImageLink(i);
		e.setId(id);
		e.setParentId(pid);
		return e;
	}
	
}
