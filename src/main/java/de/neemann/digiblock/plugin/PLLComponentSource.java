package de.neemann.digiblock.plugin;

import de.neemann.digiblock.draw.library.ComponentManager;
import de.neemann.digiblock.draw.library.ComponentSource;
import de.neemann.digiblock.draw.library.ElementLibrary;
import de.neemann.digiblock.draw.library.InvalidNodeException;
import de.neemann.digiblock.draw.shapes.GenericShape;
import de.neemann.digiblock.gui.Main;

public class PLLComponentSource implements ComponentSource {
    @Override
    public void registerComponents(ComponentManager manager) throws InvalidNodeException {
        manager.addComponent("Lattice/MachXO2", PLL.DESCRIPTION, (attributes, inputs, outputs) -> new GenericShape(
                PLL.DESCRIPTION.getName(),
                inputs,
                outputs,
                null,
                true,
                6));
    }

    public static void main(String[] args) {
        new Main.MainBuilder()
                .setLibrary(new ElementLibrary().registerComponentSource(new PLLComponentSource()))
                .openLater();
    }
}
