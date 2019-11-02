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

public class LRamDQ extends Node implements Element, RAMInterface {
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDQ.class,
            input("Clock", "clock").setClock(),
            input("ClockEn", "clockEn"),
            input("Reset", "reset"),
            input("WE", "write enable"),
            input("Address", "address"),
            input("Data", "write data")
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

    private final ObservableValue Q;
    private ObservableValue Clock;
    private ObservableValue ClockEn;
    private ObservableValue Reset;
    private ObservableValue WE;
    private ObservableValue Address;
    private ObservableValue Data;
    private int vAddress;
    private boolean vClockEn;
    private boolean read = true;
    private boolean lastClk = true; //上一个clock和当前clock与，模拟上升沿

    public LRamDQ(ElementAttributes attr) {
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
        Q = createOutput("Q");

    }


    /**
     * @param attr
     * @param size 内存大小
     * @return
     */
    protected DataField createDataField(ElementAttributes attr, int size) {
        return new DataField(size);
    }

    /**
     * getOutPuts 返回组件输出信号
     *
     * @return 输出信号
     */
    @Override
    public ObservableValues getOutputs() {
        System.out.println(Q);
        return Q.asList();
    }


    protected ObservableValue createOutput(String name) {
        return new ObservableValue(name, dataBits)
                .setToHighZ()
                .setBidirectional()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        Clock = inputs.get(0).addObserverToValue(this).checkBits(1, this);
        ClockEn = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        Reset = inputs.get(2).addObserverToValue(this).checkBits(1, this);
        WE = inputs.get(3).addObserverToValue(this).checkBits(1, this);
        Address = inputs.get(4).addObserverToValue(this).checkBits(addrBits, this);
        Data = inputs.get(5).addObserverToValue(this).checkBits(dataBits, this);
    }

    @Override
    public void readInputs() throws NodeException {
        vClockEn = ClockEn.getBool();
        boolean vClock = Clock.getBool();
        boolean vWE = WE.getBool();
        if (vClockEn) {
            if (!withOutputReg) {
                if (isNormal) {
                    if (!lastClk && vClock) {
                        vAddress = (int) Address.getValue();
                        if (vWE) {
                            long data = Data.getValue();
                            memory.setData(vAddress, data);
                            read = false;
                        } else {
                            read = true;
                        }
                    }
                    lastClk = vClock;
                }
            }
        }
    }

    @Override
    public void writeOutputs() {
        if (!withOutputReg) {
            if (isNormal) {
                boolean vClock = Clock.getBool();
                if (!lastClk && vClock) {
                    if (read) {
                        vAddress = (int) Address.getValue();
                        Q.setValue(memory.getDataWord(vAddress));
                    }
                }
                /**
                 if(!vClockEn){
                 Q.setValue(0);
                 }
                 */
                if (Reset.getBool()) {
                    Q.setValue(0);
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
