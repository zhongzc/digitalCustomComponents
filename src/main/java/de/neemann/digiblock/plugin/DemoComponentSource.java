package de.neemann.digiblock.plugin;

import de.neemann.digiblock.draw.library.ComponentManager;
import de.neemann.digiblock.draw.library.ComponentSource;
import de.neemann.digiblock.draw.library.ElementLibrary;
import de.neemann.digiblock.draw.library.InvalidNodeException;
import de.neemann.digiblock.draw.shapes.GenericShape;
import de.neemann.digiblock.gui.Main;

/**
 * Adds some components to Digiblock
 */
public class DemoComponentSource implements ComponentSource {

    /**
     * Attach your components to the simulator by calling the add methods
     *
     * @param manager the ComponentManager
     * @throws InvalidNodeException InvalidNodeException
     */
    @Override
    public void registerComponents(ComponentManager manager) throws InvalidNodeException {

        // add a component and use the default shape
        manager.addComponent("my folder/my sub folder", MyAnd.DESCRIPTION);

        // add a component and also provide a custom shape
        manager.addComponent("my folder/my sub folder", MyOr.DESCRIPTION, MyOrShape::new);

        // add a component and use the default shape
        manager.addComponent("my folder/my sub folder", MultiNot.DESCRIPTION);

        // add a component and use the default shape
        manager.addComponent("my folder/RAM", MultiPortRAM.DESCRIPTION,
                (attr, inputs, outputs) ->
                        new GenericShape("RAM", inputs, outputs, attr.getLabel(), true, 5));
    }

    /**
     * Start Digiblock with this ComponentSource attached to make debugging easier.
     * IMPORTANT: Remove the jar from Digiblocks settings!!!
     *
     * @param args args
     */
    public static void main(String[] args) {
        new Main.MainBuilder()
                .setLibrary(new ElementLibrary().registerComponentSource(new DemoComponentSource()))
                .openLater();
    }
}
