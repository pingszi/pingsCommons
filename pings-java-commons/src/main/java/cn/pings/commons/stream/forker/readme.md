### 说明：以并发方式在同一个流上执行多种操作；
- 示例：
```
public static void main(String[] args) {
    Stream<Dish> menuStream = Dish.menu.stream();

    //**使用流创建StreamForker，添加多个以key标记的操作；
    Results results = new StreamForker<Dish>(menuStream)
        .fork("shortMenu", s -> s.map(Dish::getName).collect(joining(", ")))
        .fork("totalCalories", s -> s.mapToInt(Dish::getCalories).sum())
        .fork("mostCaloricDish", s -> s.collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2)).get())
        .fork("dishesByType", s -> s.collect(groupingBy(Dish::getType)))
        .getResults();

    //**根据key获取结果
    String shortMenu = results.get("shortMenu");
    int totalCalories = results.get("totalCalories");
    Dish mostCaloricDish = results.get("mostCaloricDish");
    Map<Dish.Type, List<Dish>> dishesByType = results.get("dishesByType");

    //**打印结果
    System.out.println("Short menu: " + shortMenu);
    System.out.println("Total calories: " + totalCalories);
    System.out.println("Most caloric dish: " + mostCaloricDish);
    System.out.println("Dishes by type: " + dishesByType);
}
```