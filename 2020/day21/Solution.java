import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Solution
{
  private class Food
  {
    final String[] ingredients;
    final String[] allergens;

    private Food(final String[] ingredients, final String[] allergens)
    {
      this.ingredients = ingredients;
      this.allergens = allergens;
    }
  }

  private final ArrayList<Food> foods;
  private final TreeMap<String, Set<String>> allergen2ingredients;

  private Solution()
  {
    foods = new ArrayList<Food>();
    allergen2ingredients = new TreeMap<String, Set<String>>();
  }

  private boolean removeAllergen(final String ingredient,
                                 final String exceptForIngredient)
  {
    boolean modified = false;
    for (final String allergen : allergen2ingredients.keySet()) {
      if (allergen.equals(exceptForIngredient)) continue;
      final Set<String> ingredients = allergen2ingredients.get(allergen);
      modified |= ingredients.contains(ingredient);
      ingredients.remove(ingredient);
    }
    return modified;
  }

  private boolean removeUniqueAllergens()
  {
    boolean modified = false;
    for (final String allergen : allergen2ingredients.keySet()) {
      final Set<String> ingredients = allergen2ingredients.get(allergen);
      if (ingredients.size() == 1) {
        modified |= removeAllergen(ingredients.iterator().next(), allergen);
      }
    }
    return modified;
  }

  private int removeKnownIngredientsWithAllergens()
  {
    int safeIngredients = 0;
    for (final Food food : foods) {
      final Set<String> foodIngredients = new TreeSet<String>();
      foodIngredients.addAll(Arrays.asList(food.ingredients));
      for (final Set<String> ingredients : allergen2ingredients.values()) {
        if (ingredients.size() == 1) {
          foodIngredients.remove(ingredients.iterator().next());
        }
      }
      safeIngredients += foodIngredients.size();
    }
    return safeIngredients;
  }

  private String buildDangerousIngredientList()
  {
    final StringBuffer list = new StringBuffer();
    for (final Set<String> ingredients : allergen2ingredients.values()) {
      if (ingredients.size() == 1) {
        if (list.length() > 0) list.append(",");
        list.append(ingredients.iterator().next());
      }
    }
    return list.toString();
  }

  private Food parseFood(final String unparsed)
  {
    final String[] tokens = unparsed.split(" \\(contains ");
    final String[] ingredients = tokens[0].split(" ");
    final String[] allergens =
      tokens[1].substring(0, tokens[1].length() - 1).split(", ");
    for (final String allergen : allergens) {
      if (allergen2ingredients.containsKey(allergen)) {
        allergen2ingredients.get(allergen).
          retainAll(Arrays.asList(ingredients));
      } else {
        final Set<String> possibleIngredients = new TreeSet<String>();
        possibleIngredients.addAll(Arrays.asList(ingredients));
        allergen2ingredients.put(allergen, possibleIngredients);
      }
    }
    return new Food(ingredients, allergens);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    String line;
    while ((line = reader.readLine()) != null) {
      foods.add(parseFood(line));
    }
    for (boolean modified = true; modified;) {
      modified = removeUniqueAllergens();
    }
    System.out.println(removeKnownIngredientsWithAllergens()); // part 1
    System.out.println(buildDangerousIngredientList()); // part 2
  }

  public static void main(final String argv[]) throws IOException
  {
    new Solution().run("data.txt");
  }
}
