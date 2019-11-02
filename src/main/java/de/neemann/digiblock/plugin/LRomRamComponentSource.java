package de.neemann.digiblock.plugin;

import de.neemann.digiblock.draw.library.ComponentManager;
import de.neemann.digiblock.draw.library.ComponentSource;
import de.neemann.digiblock.draw.library.ElementLibrary;
import de.neemann.digiblock.draw.library.InvalidNodeException;
import de.neemann.digiblock.gui.Main;

/**
 * 添加 RAM 和ROM组件到digital
 */
public class LRomRamComponentSource implements ComponentSource {
    /**
     * 调用此方法将自己的组件添加到模拟器中
     *
     * @param manager  组件管理器
     * @throws InvalidNodeException
     *
     */
    @Override
    public void registerComponents(ComponentManager manager)throws InvalidNodeException {
        manager.addComponent("Lattice/MachXO2", LRamDPTrue.DESCRIPTION);
        manager.addComponent("Lattice/MachXO2", LRamDQ.DESCRIPTION);
        manager.addComponent("Lattice/MachXO2", LRom.DESCRIPTION);
        manager.addComponent("Lattice/MachXO2", LRamDP.DESCRIPTION);
        manager.addComponent("Lattice/MachXO2", RegsFile.DESCRIPTION);
    }

    public static void main(String[] args)  {
        new Main.MainBuilder()
                .setLibrary(new ElementLibrary().registerComponentSource(new LRomRamComponentSource()))
                .openLater();
    }

}