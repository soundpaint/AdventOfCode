import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Part1
{
  private Part1() {}

  int pc = 0;
  int acc = 0;

  public class OpCode
  {
    String code;
    int arg;
  }

  private void execute(OpCode opCode) {
    System.out.println(pc + ": " + opCode.code + " " + opCode.arg + " (acc=" + acc + ")");
    switch (opCode.code) {
    case "acc" :
      pc++;
      acc += opCode.arg;
      break;
    case "jmp":
      pc += opCode.arg;
      break;
    case "nop":
      pc++;
      break;
    default:
      throw new RuntimeException();
    }
  }

  private void printPrg(OpCode[] prg) {
    for (int i = 0; i < prg.length; i++) {
      System.out.println(prg[i].code + " " + prg[i].arg);
    }
  }

  private void run1(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    OpCode[] prg = new OpCode[values.size()];
    int ln = 0;
    for (final var value : values) {
      OpCode opCode = new OpCode();
      prg[ln++] = opCode;
      String op[] = value.split(" ");
      opCode.code = op[0];
      System.out.println(op[1]);
      opCode.arg = Integer.parseInt(op[1]);
    }
    boolean seen[] = new boolean[prg.length];
    for (int i = 0; i < prg.length; i++) {
      System.out.println("i=" + i);
      OpCode opCode = prg[i];
      if (opCode.code.equals("nop")) {
        opCode.code = "jmp";
      } else if (opCode.code.equals("jmp")) {
        opCode.code = "nop";
      } else {
        System.out.println("skipping " + i);
        continue;
      }
      printPrg(prg);
      pc = 0;
      acc = 0;
      for (int j = 0; j < seen.length; j++) {
        seen[j] = false;
      }
      while ((pc != prg.length) && !seen[pc]) {
        seen[pc] = true;
        execute(prg[pc]);
      }
      if (pc == prg.length) {
        System.out.println("acc=" + acc);
        break;
      }
      if (opCode.code.equals("nop")) {
        opCode.code = "jmp";
      } else if (opCode.code.equals("jmp")) {
        opCode.code = "nop";
      }
    }
    // System.out.println("acc=" + acc);
    // 949
  }

  private void run2(final String filePath) throws IOException
  {
    final var path = Paths.get(filePath);
    final var data =
      new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    final String values[] = data.trim().split("[,\\s]");
    for (final var value : values) {
      System.out.println("[" + value + "]");
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run1("data.txt");
  }
}
