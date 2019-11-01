package de.neemann.digiblock.plugin;

import de.neemann.digiblock.core.Node;
import de.neemann.digiblock.core.NodeException;
import de.neemann.digiblock.core.ObservableValue;
import de.neemann.digiblock.core.ObservableValues;
import de.neemann.digiblock.core.element.*;

import static de.neemann.digiblock.core.element.PinInfo.input;

/**
 * A custom Or component
 */
public class MyOr extends Node implements Element {

    static final Key<Integer> ELLIPSE_SIZE =
            new Key.KeyInteger("ellipseSize", 2)
                    .setMin(1)
                    .setMax(5)
                    .setComboBoxValues(1, 2, 3, 4, 5)
                    .setName("Ellipse Size")
                    .setDescription("Sets the size of the ellipse.");

    /**
     * The description of the new component
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(MyOr.class,
            input("a", "or input a"),
            input("b", "or input b"))
            .addAttribute(Keys.ROTATE)   // allows to rotate the new component
            .addAttribute(Keys.BITS)     // allows to set a bit number to the component
            .addAttribute(ELLIPSE_SIZE); // the size of the ellipse

    private final int bits;
    private final ObservableValue out;
    private ObservableValue a;
    private ObservableValue b;
    private long outValue;

    /**
     * Creates a component.
     * The constructor is able to access the components attributes and has
     * to create the components output signals, which are instances of the {@link ObservableValue} class.
     * As soon as the constructor is called you have to expect a call to the getOutputs() method.
     *
     * @param attr the attributes which are editable in the components properties dialog
     */
    public MyOr(ElementAttributes attr) {
        bits = attr.getBits();
        out = new ObservableValue("out", bits).setDescription("The components single output");
    }

    /**
     * This method is called if one of the input values has changed.
     * Here you can read the input values of your component.
     * It is not allowed to write to one of the outputs!!!
     */
    @Override
    public void readInputs() {
        long valueA = a.getValue();
        long valueB = b.getValue();
        outValue = valueA | valueB;
    }

    /**
     * This method is called if you have to update your output.
     * It is not allowed to read one of the inputs!!!
     */
    @Override
    public void writeOutputs() {
        out.setValue(outValue);
    }

    /**
     * This method is called to register the input signals which are
     * connected to your components inputs. The order is the same as given in
     * the {@link ElementTypeDescription}.
     * You can store the instances, make some checks and so on.
     * IMPORTANT: If it's necessary that your component is called if the input
     * changes, you have to call the addObserverToValue method on that input.
     * If a combinatorial component is implemented you have to add the observer
     * to all inputs. If your component only reacts on a clock signal you only
     * need to add the observer to the clock signal.
     *
     * @param inputs the list of <code>ObservableValue</code>s to use
     * @throws NodeException NodeException
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this);
    }

    /**
     * This method must return the output signals of your component.
     *
     * @return the output signals
     */
    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(out);
    }
}
