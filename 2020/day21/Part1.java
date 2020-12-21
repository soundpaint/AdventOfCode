import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Part1
{
  private class Food
  {
    String[] ingredients;
    String[] allergens;
    Set<String> reducedIngredients;

    private void printReducedIngredients()
    {
      final StringBuffer s = new StringBuffer();
      for (final String ingredient : reducedIngredients) {
        if (s.length() > 0) s.append(" ");
        s.append(ingredient);
      }
      System.out.println(s);
    }
  }

  private final ArrayList<Food> foods;
  private final TreeMap<String, Set<String>> allergen2possibleIngredients;

  private Part1()
  {
    foods = new ArrayList<Food>();
    allergen2possibleIngredients = new TreeMap<String, Set<String>>();
  }

  private void printAllergen2possibleIngredients()
  {
    for (final String key : allergen2possibleIngredients.keySet()) {
      System.out.println("allergen " + key + ":");
      final Set<String> ingredients = allergen2possibleIngredients.get(key);
      for (final String ingredient : ingredients) {
        System.out.println("in " + ingredient);
      }
    }
  }

  private boolean removeAllergen(String ingredient, String exceptForIngredient)
  {
    boolean modified = false;
    for (final String key : allergen2possibleIngredients.keySet()) {
      if (key.equals(exceptForIngredient)) continue;
      final Set<String> ingredients = allergen2possibleIngredients.get(key);
      modified |= ingredients.contains(ingredient);
      ingredients.remove(ingredient);
    }
    return modified;
  }

  private boolean removeUniqueAllergens()
  {
    boolean modified = false;
    for (final String key : allergen2possibleIngredients.keySet()) {
      final Set<String> ingredients = allergen2possibleIngredients.get(key);
      if (ingredients.size() == 1) {
        modified |= removeAllergen(ingredients.iterator().next(), key);
      }
    }
    return modified;
  }

  private void removeKnownIngredientsWithAllergens()
  {
    int sum = 0;
    for (final Food food : foods) {
      final Set<String> foodIngredients = new TreeSet<String>();
      foodIngredients.addAll(Arrays.asList(food.ingredients));
      for (final String key : allergen2possibleIngredients.keySet()) {
        final Set<String> ingredients = allergen2possibleIngredients.get(key);
        if (ingredients.size() == 1) {
          foodIngredients.remove(ingredients.iterator().next());
        }
      }
      food.reducedIngredients = foodIngredients;
      sum += foodIngredients.size();
    }
    // Result of Part 1
    System.out.println(sum);
  }

  private void printPart2Result()
  {
    final StringBuffer s = new StringBuffer();
    for (final String key : allergen2possibleIngredients.keySet()) {
      final Set<String> ingredients = allergen2possibleIngredients.get(key);
      if (ingredients.size() == 1) {
        if (s.length() > 0) s.append(",");
        s.append(ingredients.iterator().next());
      }
    }
    System.out.println(s);
  }

  private void parseFood(final ArrayList<String> lines)
  {
    for (final var line : lines) {
      final String[] tokens = line.split(" \\(contains ");
      final String strIngredients = tokens[0];
      final String[] ingredients = strIngredients.split(" ");
      final String[] allergens;
      if (tokens.length > 1) {
        final String strAllergens =
          tokens[1].substring(0, tokens[1].length() - 1);
        allergens = strAllergens.split(", ");
        for (final String allergen : allergens) {
          if (allergen2possibleIngredients.containsKey(allergen)) {
            allergen2possibleIngredients.get(allergen).
              retainAll(Arrays.asList(ingredients));
          } else {
            final Set<String> possibleIngredients = new TreeSet<String>();
            possibleIngredients.addAll(Arrays.asList(ingredients));
            allergen2possibleIngredients.put(allergen, possibleIngredients);
          }
        }
      } else {
        allergens = null;
      }
      final Food food = new Food();
      food.ingredients = ingredients;
      food.allergens = allergens;
      foods.add(food);
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
    parseFood(lines);
    printAllergen2possibleIngredients();
    System.out.println("--------");
    boolean modified;
    do {
      modified = removeUniqueAllergens();
    } while (modified);
    printAllergen2possibleIngredients();
    System.out.println("--------");
    removeKnownIngredientsWithAllergens();
    printPart2Result();
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
