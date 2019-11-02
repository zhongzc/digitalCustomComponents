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

public class LRamDP extends Node implements Element, RAMInterface {
    /**
     * 新组件描述
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDP.class,
            input("WrAddress", "input Write Address"),
            input("RdAddress", "input Read Address"),
            input("Data", "input write Data"),
            input("WE", "input WE"),
            input("RdClockEn", "input RdClockEn"),
            input("RdClock", "input RdClk").setClock(),
            input("WrClockEn", "input WrClockEn"),
            input("WrClock", "input WrClock").setClock(),
            input("Reset", "reset").setClock()
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
    private ObservableValue WrAdd;
    private ObservableValue RdAdd;
    private ObservableValue Data;
    private ObservableValue WE;
    private ObservableValue RdClkEn;
    private ObservableValue WrClkEn;
    private ObservableValue WrClk;
    private ObservableValue RdClk;
    private ObservableValue Reset;

    private boolean lastWClock = false;
    private boolean lastRClock = false;

    /**
     * 创建一个组件。
     * 构造器可以获取组件属性，同时必须创建组件输出信号，
     * 输出信号是一个ObservableValue类的实例。
     * 当构造器被调用的时候需要一个getOutputs()方法
     *
     * @param attr attr在组建属性框中可以进行编辑
     */

    public LRamDP(ElementAttributes attr) {
        super(true);
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        label = attr.getLabel();
        size = 1 << addrBits;
        memory = createDataField(attr, size);
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        Q = createOutput();
        isNormal = attr.get(LatticeKeys.IS_NORMAL);
        isReadBWrite = attr.get(LatticeKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(LatticeKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(LatticeKeys.WITH_OUTPUT_REG);
    }

    /**
     * 构建输出端口
     *
     * @return
     */
    protected ObservableValue createOutput() {
        return new ObservableValue("Q", dataBits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
    }

    /**
     * 注册输入信号，输入顺序和ElementTypeDescription中描述的顺序一致
     * 注意：如果当输入信号发生变化时需要调用组件，必须为组件的所有输入添加addObserverToValue方法
     *
     * @param inputs 可见值列表
     * @throws NodeException
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        WrAdd = inputs.get(0).addObserverToValue(this).checkBits(addrBits, this);
        RdAdd = inputs.get(1).addObserverToValue(this).checkBits(addrBits, this);
        Data = inputs.get(2).addObserverToValue(this).checkBits(dataBits, this);
        WE = inputs.get(3).addObserverToValue(this).checkBits(1, this);
        RdClkEn = inputs.get(4).addObserverToValue(this).checkBits(1, this);
        RdClk = inputs.get(5).addObserverToValue(this).checkBits(1, this);
        WrClkEn = inputs.get(6).addObserverToValue(this).checkBits(1, this);
        WrClk = inputs.get(7).addObserverToValue(this).checkBits(1, this);
        Reset = inputs.get(8).addObserverToValue(this).checkBits(1, this);
    }

    /**
     * readInput 方法，当组件输入的值有改变的时候调用
     */
    @Override
    public void readInputs() throws NodeException {
        boolean vWrClkEn = WrClkEn.getBool();
        boolean vWrClk = WrClk.getBool();
        boolean vWE = WE.getBool();
        if (!withOutputReg) {
            if (isNormal) {
                if (vWrClk && !lastWClock) {
                    if (vWE && vWrClkEn) {
                        int vWrAddress = (int) WrAdd.getValue();
                        long data = Data.getValue();
                        memory.setData(vWrAddress, data);
                    }
                }
                lastWClock = vWrClk;
            }
        }
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
        return Q.asList();
    }

    @Override
    public void writeOutputs() {
        boolean vRdClkEn = RdClkEn.getBool();
        boolean vRdClk = RdClk.getBool();
        if (!withOutputReg) {
            if (isNormal) {
                if (vRdClk && !lastRClock) {
                    if (vRdClkEn) {
                        int vRdAdd = (int) RdAdd.getValue();
                        Q.setValue(memory.getDataWord(vRdAdd));
                    }
                }
                if (Reset.getBool()) {
                    Q.setValue(0);
                }
                lastRClock = vRdClk;
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
