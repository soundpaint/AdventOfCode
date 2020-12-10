import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Part2
{
  private Part2() {}

  int pc = 0;
  int acc = 0;

  private class AbstractInstruction
  {
    private int mnemonic;
    private int arg;

    private AbstractInstruction() {
      throw new UnsupportedOperationException("empty constructor");
    }

    protected AbstractInstruction(final String mnemonic, final String arg)
    {
      this.mneminic = mnemonic;
      this.arg = arg;
    }

    void execute();

    public String tostring()
    {
      System.out.println(mnemonic + " " + arg);
    }
  }

  private class ACC extends AbstractInstruction {
    ACC(final String arg)
    {
      super(arg);
    }
    public void execute()
    {
      pc++;
      acc += opCode.arg;
    }
  }

  private class JMP extends AbstractInstruction {
    JMP(final String arg)
    {
      super(arg);
    }
    public void execute()
    {
      pc += opCode.arg;
    }
  }

  private class NOP extends AbstractInstruction {
    NOP(final String arg)
    {
      super(arg);
    }
    public void execute()
    {
      pc++;
    }
  }

  public static Instruction parse(final String unparsed)
  {
    final String tokens[] = unparsed.split(" ");
    final String arg = Integer.parseInt(tokens[1]);
    final Instruction instruction;
    switch (tokens[0]) {
    case "acc" :
      instruction = new ACC(arg);
      break;
    case "jmp":
      instruction = new JMP(arg);
      break;
    case "nop":
      instruction = new NOP(arg);
      break;
    default:
      throw new RuntimeException();
    }
    return instruction;
  }

  private void printPrg(final Instruction[] prg) {
    for (int adr = 0; adr < prg.length; adr++) {
      System.out.println(prg[adr]);
    }
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var lines = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }
    Instruction[] prg = new Instruction[lines.size()];
    int adr = 0;
    for (final var line : lines) {
      final Instruction instruction = new Instruction(line);
      prg[adr++] = instruction;
    }
    boolean seen[] = new boolean[prg.length];
    for (int adr = 0; adr < prg.length; adr++) {
      System.out.println("adr=" + adr);
      Instruction tmpInstruction;
      Instruction instruction = prg[adr];
      if (instruction instanceof NOP) {
        tmpInstruction = new JMP(instruction.getArg());
      } else if (instruction instanceof JMP) {
        tmpInstruction = new NOP(instruction.getArg());
      } else {
        System.out.println("skipping " + i);
        continue;
      }
      prg[adr] = tmpInstruction;
      printPrg(prg);
      pc = 0;
      acc = 0;
      for (int adr = 0; adr < seen.length; adr++) {
        seen[adr] = false;
      }
      while ((pc != prg.length) && !seen[pc]) {
        seen[pc] = true;
        execute(prg[pc]);
      }
      if (pc == prg.length) {
        System.out.println("acc=" + acc);
        break;
      }
      prg[adr] = instruction;
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
