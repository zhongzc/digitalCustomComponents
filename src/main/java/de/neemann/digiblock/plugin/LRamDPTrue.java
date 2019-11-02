package de.neemann.digiblock.plugin;

import de.neemann.digiblock.core.LatticeKeys;
import de.neemann.digiblock.core.Node;
import de.neemann.digiblock.core.NodeException;
import de.neemann.digiblock.core.ObservableValue;
import de.neemann.digiblock.core.ObservableValues;
import de.neemann.digiblock.core.element.Element;
import de.neemann.digiblock.core.element.ElementAttributes;
import de.neemann.digiblock.core.element.ElementTypeDescription;
import de.neemann.digiblock.core.element.Keys;
import de.neemann.digiblock.core.memory.DataField;
import de.neemann.digiblock.core.memory.RAMInterface;

import static de.neemann.digiblock.core.element.PinInfo.input;

public class LRamDPTrue extends Node implements Element, RAMInterface {
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDPTrue.class,
            input("DataInA", "data input a"),
            input("DataInB", "data input b"),
            input("AddressA", "address a"),
            input("AddressB", "address b"),
            input("ClockA", "clock a").setClock(),
            input("ClockB", "clock b").setClock(),
            input("ClockEnA", "clock a enable "),
            input("ClockEnB", "clock b enable"),
            input("WrA", "write enable port a"),
            input("WrB", "write enable port b"),
            input("ResetA", "reset a"),
            input("ResetB", "reset b")
    )
            .addAttribute(Keys.ROTATE)  //允许旋转新组建
            .addAttribute(Keys.BITS)    //设置组件bit数
            .addAttribute(Keys.ADDR_BITS) //设置组件地址bit数
            .addAttribute(Keys.LABEL)    //设置组件标签
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(LatticeKeys.IS_NORMAL)
            .addAttribute(LatticeKeys.IS_READ_BEFORE_WRITE)
            .addAttribute(LatticeKeys.IS_WRITE_THROUGH)
            .addAttribute(LatticeKeys.WITH_OUTPUT_REG);

    private final int dataBits;
    private final int addrBits;
    private final String label;
    private final int size;
    private final boolean isProgramMemory;
    private final boolean isNormal;
    private final boolean isReadBWrite;
    private final boolean isWriteThrough;
    private final boolean withOutputReg;
    private DataField memory;

    private final ObservableValue QA;
    private final ObservableValue QB;

    private ObservableValue DataInA;
    private ObservableValue DataInB;
    private ObservableValue AddressA;
    private ObservableValue AddressB;
    private ObservableValue ClockA;
    private ObservableValue ClockB;
    private ObservableValue ClockEnA;
    private ObservableValue ClockEnB;
    private ObservableValue WrA;
    private ObservableValue WrB;
    private ObservableValue ResetA;
    private ObservableValue ResetB;

    private boolean readA = true;
    private boolean readB = true;
    private boolean lastClockA = true;
    private boolean lastClockB = true;
    private int vAddressA;
    private int vAddressB;
    private boolean vClockEnA;
    private boolean vClockEnB;

    public LRamDPTrue(ElementAttributes attr) {
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        label = attr.getLabel();
        size = 1 << addrBits;
        memory = createDataField(attr, size);
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        isNormal = attr.get(LatticeKeys.IS_NORMAL);
        isReadBWrite = attr.get(LatticeKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(LatticeKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(LatticeKeys.WITH_OUTPUT_REG);
        QA = createOutput("QA");
        QB = createOutput("QB");
    }

    protected DataField createDataField(ElementAttributes attr, int size) {
        return new DataField(size);
    }

    @Override
    public ObservableValues getOutputs() {
        ObservableValues Q = new ObservableValues(new ObservableValue[]{QA, QB});
        return Q;
    }


    protected ObservableValue createOutput(String name) {
        return new ObservableValue(name, dataBits)
                .setToHighZ()
                .setBidirectional()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        DataInA = inputs.get(0).addObserverToValue(this).checkBits(dataBits, this);
        DataInB = inputs.get(1).addObserverToValue(this).checkBits(dataBits, this);
        AddressA = inputs.get(2).addObserverToValue(this).checkBits(addrBits, this);
        AddressB = inputs.get(3).addObserverToValue(this).checkBits(addrBits, this);
        ClockA = inputs.get(4).addObserverToValue(this).checkBits(1, this);
        ClockB = inputs.get(5).addObserverToValue(this).checkBits(1, this);
        ClockEnA = inputs.get(6).addObserverToValue(this).checkBits(1, this);
        ClockEnB = inputs.get(7).addObserverToValue(this).checkBits(1, this);
        WrA = inputs.get(8).addObserverToValue(this).checkBits(1, this);
        WrB = inputs.get(9).addObserverToValue(this).checkBits(1, this);
        ResetA = inputs.get(10).addObserverToValue(this).checkBits(1, this);
        ResetB = inputs.get(11).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean vClockA = ClockA.getBool();
        boolean vClockB = ClockB.getBool();
        boolean vWrA = WrA.getBool();
        boolean vWrB = WrB.getBool();
        vClockEnA = ClockEnA.getBool();
        vClockEnB = ClockEnB.getBool();
        if (!withOutputReg) {
            if (isNormal) {
                if (vClockEnA) {
                    if (vClockA && !lastClockA) {
                        vAddressA = (int) AddressA.getValue();
                        if (vWrA) {
                            long dataA = DataInA.getValue();
                            memory.setData(vAddressA, dataA);
                            readA = false;
                        } else {
                            readA = true;
                        }
                    }
                } else {
                    readA = false;
                }
                if (vClockEnB) {
                    if (vClockB && !lastClockB) {
                        vAddressB = (int) AddressB.getValue();
                        if (vWrB) {
                            long dataB = DataInB.getValue();
                            memory.setData(vAddressB, dataB);
                            readB = false;
                        } else {
                            readB = true;
                        }
                    }
                } else {
                    readB = false;
                }
            }
            lastClockA = vClockA;
        }
    }

    @Override
    public void writeOutputs() {
        if (!withOutputReg) {
            if (isNormal) {
                if (readA) {
                    vAddressA = (int) AddressA.getValue();
                    QA.setValue(memory.getDataWord(vAddressA));
                }
                if (readB) {
                    vAddressB = (int) AddressB.getValue();
                    QB.setValue(memory.getDataWord(vAddressB));
                }
                if (ResetA.getBool()) {
                    QA.setValue(0);
                }
                if (ResetB.getBool()) {
                    QB.setValue(0);
                }
            }
        }
    }

    public void setData(DataField data) {
        memory = data;
    }

    @Override
    public DataField getMemory() {
        return memory;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getDataBits() {
        return dataBits;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    @Override
    public void setProgramMemory(DataField dataField) {
        memory.setDataFrom(dataField);
    }


}
