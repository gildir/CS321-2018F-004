/**
 *
 * @author Kevin
 */
public enum Direction {
    NORTH("North"), SOUTH("South"), EAST("East"), WEST("West");
    
    private String directionName;
    
    Direction(String name) {
        directionName = name;
    }
    
    public static Direction toValue(String string) {
       string = string.toUpperCase();
        if(string != null) {
            switch(string) {
                case "N": string = "NORTH"; break; 
                case "E": string = "EAST"; break;
                case "S": string = "SOUTH"; break;
                case "W": string = "WEST"; break;
            }
            for(Direction direction : Direction.values()) {
                if(string.equalsIgnoreCase(direction.directionName)) {
                    return direction;
                }
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return directionName;
    }
    
}
