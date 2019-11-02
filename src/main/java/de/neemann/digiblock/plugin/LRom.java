package de.neemann.digiblock.plugin;

import de.neemann.digiblock.core.LatticeKeys;
import de.neemann.digiblock.core.*;
import de.neemann.digiblock.core.element.Element;
import de.neemann.digiblock.core.element.ElementAttributes;
import de.neemann.digiblock.core.element.ElementTypeDescription;
import de.neemann.digiblock.core.element.Keys;
import de.neemann.digiblock.core.memory.DataField;
import de.neemann.digiblock.core.memory.ProgramMemory;
import de.neemann.digiblock.core.memory.importer.Importer;
import de.neemann.digiblock.core.memory.rom.ROMInterface;

import java.io.File;
import java.io.IOException;

import static de.neemann.digiblock.core.element.PinInfo.input;

/**
 * Rom组件
 */
public class LRom extends Node implements Element, ROMInterface, ProgramMemory {
    public final static String LAST_DATA_FILE_KEY = "lastDataFile";
    /**
     * 新组件描述
     */
    public static final ElementTypeDescription DESCRIPTION=new ElementTypeDescription(LRom.class,
            input("Address", "input Address"),
            input("OutClock", "input outClock").setClock(),
            input("OutClockEn", "input outClockEn"),
            input("Reset", "input reset")
             )
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(LatticeKeys.IS_NORMAL)
            .addAttribute(LatticeKeys.IS_READ_BEFORE_WRITE)
            .addAttribute(LatticeKeys.IS_WRITE_THROUGH)
            .addAttribute(LatticeKeys.WITH_OUTPUT_REG)
            .addAttribute(Keys.AUTO_RELOAD_ROM);

    private final IntFormat intFormat;
    private final int dataBits;
    private final int addrBits;
    private final String label;
    private DataField data;
    private final File hexFile;
    private final boolean autoLoad;
    private final boolean isProgramMemory;
    private final boolean isNormal;
    private final boolean isReadBWrite;
    private final boolean isWriteThrough;
    private final boolean withOutputReg;
    private ObservableValue Q;
    private ObservableValue Addr ;
    private ObservableValue OClk;
    private ObservableValue OClkEn;
    private ObservableValue Reset;
    private int vAddr;
    private boolean vOClkEn;
    private boolean vOClk;
    private  boolean vReset;
    private boolean lastClock=true;

    public LRom(ElementAttributes attr){
        dataBits=attr.get(Keys.BITS);
        addrBits=attr.get(Keys.ADDR_BITS);
        label=attr.getLabel();
        data = attr.get(Keys.DATA);
        autoLoad = attr.get(Keys.AUTO_RELOAD_ROM);
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        isNormal = attr.get(LatticeKeys.IS_NORMAL);
        isReadBWrite = attr.get(LatticeKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(LatticeKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(LatticeKeys.WITH_OUTPUT_REG);
        if (autoLoad) {
            hexFile = attr.getFile(LAST_DATA_FILE_KEY);
        } else
            hexFile = null;
        Q=createOutput();
        intFormat = attr.get(Keys.INT_FORMAT);
    }

    protected ObservableValue createOutput(){
        return new ObservableValue("Q",dataBits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        Addr=inputs.get(0).addObserverToValue(this).checkBits(addrBits,this);
        OClk = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        OClkEn = inputs.get(2).addObserverToValue(this).checkBits(1, this);
        Reset=inputs.get(3).addObserverToValue(this).checkBits(1,this);
    }

    @Override
    public void readInputs(){
        vAddr=(int)Addr.getValue();
        vOClkEn=OClkEn.getBool();
        vOClk=OClk.getBool();
        vReset=Reset.getBool();
    }

    @Override
    public ObservableValues getOutputs() {
        return Q.asList();
    }

    @Override
    public void writeOutputs(){
        if(!withOutputReg){
            if(isNormal){
                    if(vOClk && !lastClock){
                        if(vOClkEn){
                        vAddr=(int)Addr.getValue();
                       Q.setValue(data.getDataWord(vAddr));
                    }

                }
            }
            if(vReset){
                Q.setValue(0);
            }
            lastClock=vOClk;
        }
    }
    @Override
    public void init(Model model) throws NodeException {
        if (autoLoad) {
            try {
                data = Importer.read(hexFile, dataBits);
            } catch (IOException e) {
                throw new NodeException(e.getMessage(), this, -1, null);
            }
        }
    }
    @Override
    public void setProgramMemory(DataField dataField) {
        setData(dataField);
    }

    /**
     * @return true if this is program memory
     */
    @Override
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    @Override
    public void setData(DataField data) {
        this.data = data;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public IntFormat getIntFormat() {
        return intFormat;
    }

    @Override
    public int getDataBits() {
        return dataBits;
    }

}
