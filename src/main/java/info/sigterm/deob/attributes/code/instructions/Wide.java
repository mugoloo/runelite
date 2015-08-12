package info.sigterm.deob.attributes.code.instructions;

import info.sigterm.deob.attributes.code.Instruction;
import info.sigterm.deob.attributes.code.InstructionType;
import info.sigterm.deob.attributes.code.Instructions;
import info.sigterm.deob.attributes.code.instruction.types.LVTInstruction;
import info.sigterm.deob.attributes.code.instruction.types.WideInstruction;
import info.sigterm.deob.execution.Frame;
import info.sigterm.deob.execution.InstructionContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class Wide extends Instruction implements LVTInstruction
{
	private Instruction ins;

	public Wide(Instructions instructions, InstructionType type, int pc) throws IOException
	{
		super(instructions, type, pc);

		DataInputStream is = instructions.getCode().getAttributes().getStream();

		byte opcode = is.readByte(); // this byte is already in the length of the new instruction (length is initialized to 1)
		InstructionType op = InstructionType.findInstructionFromCode(opcode);
		
		try
		{
			Constructor<? extends Instruction> con = op.getInstructionClass().getConstructor(Instructions.class, InstructionType.class, Instruction.class, int.class);
			ins = con.newInstance(instructions, op, this, pc);
			length += ins.getLength();
		}
		catch (Exception ex)
		{
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException
	{
		super.write(out);
		
		WideInstruction w = (WideInstruction) ins;
		w.writeWide(out);
	}

	@Override
	public void execute(Frame frame)
	{
		InstructionContext ctx = new InstructionContext(this, frame);
		
		ins.execute(frame);
		
		frame.addInstructionContext(ctx);
	}

	@Override
	public void replace(Instruction oldi, Instruction newi)
	{
		assert oldi != ins;
	}

	@Override
	public int getVariableIndex()
	{
		return ((LVTInstruction) ins).getVariableIndex();
	}

	@Override
	public Instruction setVariableIndex(int idx)
	{
		ins = ((LVTInstruction) ins).setVariableIndex(idx);
		return this;
	}

	@Override
	public boolean store()
	{
		return ((LVTInstruction) ins).store();
	}
}
