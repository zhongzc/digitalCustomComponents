package de.neemann.digiblock.plugin;

import de.neemann.digiblock.core.Node;
import de.neemann.digiblock.core.NodeException;
import de.neemann.digiblock.core.ObservableValue;
import de.neemann.digiblock.core.ObservableValues;
import de.neemann.digiblock.core.element.*;

public class PLL extends Node implements Element {
    public static final ElementTypeDescription DESCRIPTION;
    private final ObservableValues outs;
    private ObservableValue CLKI;
    private long outValue;

    public PLL(ElementAttributes attr) {
        Boolean clkos = (Boolean)attr.get(new Key("CLKOS", false));
        if (clkos) {
            this.outs = new ObservableValues(new ObservableValue[]{new ObservableValue("CLKOP", 1), new ObservableValue("CLKOS", 1)});
        } else {
            this.outs = (new ObservableValue("CLKOP", 1)).asList();
        }

    }

    public void readInputs() {
        this.outValue = this.CLKI.getValue();
    }

    public void writeOutputs() {
        this.outs.forEach((o) -> {
            o.setValue(this.outValue);
        });
    }

    public void setInputs(ObservableValues inputs) throws NodeException {
        this.CLKI = inputs.get(0).addObserverToValue(this).checkBits(1, this);
    }

    public ObservableValues getOutputs() {
        return this.outs;
    }

    static {
        DESCRIPTION = (new ElementTypeDescription(PLL.class, new PinDescription[]{PinInfo.input("CLKI", "")})).addAttribute(Keys.ROTATE).addAttribute((new Key("CLKOS", false)).setName("CLKOS"));
    }
}
