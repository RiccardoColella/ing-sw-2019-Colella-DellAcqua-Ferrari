# template for attacks.json
The file should be formatted as a json array, and every element of the array should follow this pattern:<br/>
```
{
    "weaponId": "Weapon Name", 
    "basicAttack": {
        "name": "Attack Name",
        "cost": ["YELLOW", "RED"],
        "actions": [
            {
                "damage": 2,
                "targets": "FIND",
                "targetAmount": "ONE",
                "skippable": false,
                "targetPosition": "FIXED",
                "targetDistance": {"min": 1, "max": 2},
                "startingPoint": "ACTIVE_PLAYER"
            }
        ]
    } 
}
```
####**MANDATORY FIELDS IN WEAPON**<br/>
* `weaponId`: a string representing the (unique) name of the method
* `basicAttack`: an object representing the basic attack of the weapon

####**OPTIONAL FIELDS IN WEAPON**<br/>
* `alternativeAttack`: an object representing the alternative attack of the weapon. 
* `advancedAttacks`: an array of attack objects representing the powered attacks of the weapon. A weapon can have an alternative attack or the advanced attacks, but not both
* `mustExecuteInOrder`: a boolean that specifies whether the attacks of the weapon must follow the order basic, then advanced[0], then advanced[1] and so on.
It should only be used with weapons with advanced attacks. Default is `false`

####**MANDATORY FIELDS IN ATTACK**<br/>
* `name`: a string representing the name of the attack. It must be unique within the same weapon
* `actions` an array of action objects

####**OPTIONAL FIELDS IN ATTACK**<br/>
* `cost`: an array containing the color of the coins needed to pay for the attack. Default value is an empty array (free attack)
* `basicMustBeFirst`: a boolean that specifies whether this attack requires the execution of the basic attack to be used. It should only be used in weapons with advanced attacks. Default is `false`

####**MANDATORY FIELDS IN ACTION**<br/>
* `targets`: a string representing the way the targets should be computed. The supported values are: 
    * `FIND`: new targets will be computed
    * `INHERIT_LAST`: the last target hit by the weapon that is not the active player
    * `INHERIT_ALL`: all the targets previously hit by the weapon (active player excluded)
    * `SELF`: the active player
    
* one out of `damage`, `mark` and `move`: damage and mark take an `int` representing the amount of damage tokens that will be given to the target. 
Move takes a range object: `{"min": 1, "max": 2}`, a default value will be used if min or max is missing.

* `targetAmount`: a string representing the way to group potential targets in sets. The supported values are:
    * `ONE`: every target constitutes a singleton set
    * `BLOCK`: the sets are composed by all the players on the same block of the given target (active player excluded)
    * `ROOM`: the sets are composed by all the players in the same room of the given target (active player excluded)
    * `ALL`: all the targets are united into a single set
    
* `skippable`: a boolean, `true` if the action is skippable (which means the attack can end _**before**_ executing it), 
`false` if the action must be done if all the actions before it have been executed (e.g. an attack might have 3 actions, A, B and C. 
If action A is mandatory, action B is not and action C is mandatory if the attack gets to the point of executing it, A and
C are **NOT** skippable, even if C might not be executed every time, B is skippable)

####**OPTIONAL FIELDS IN ACTION**
Some of the following fields might be mandatory in particular circumstances, or should not be used in others, as further specified.

* `startingPoint`: mandatory when `targets` is set to `FIND`, but can also be used in other circumstances. The starting point
represents the block that shall be used as a base to compute available targets. The supported values are:
    * `INHERIT`: previous starting point will be inherited, if none was found an exception is thrown (this should only be used
    when it's certain that a starting point already exists)
    * `INHERIT_IF_PRESENT`: the previous starting point is used, but if it's missing it will be set to the default value `ACTIVE_PLAYER`
    * `ACTIVE_PLAYER`: the block on which the active player can be found at the moment is the starting point
    * `VISIBLE`: a block that is visible from the active player (unlike the other options, this might require user interaction)

* `targetPosition`: mandatory when `targets` is set to `FIND`, it will be ignored in all other cases. Supported values are:
   * `FIXED`: the target is at a fixed distance from the starting point (it implies that `targetDistance` **must** be specified)
   * `VISIBLE`: the target is visible from the starting point
   * `STRAIGHT`: the target is in a fixed cardinal direction from the starting point
   
* `targetDistance`: mandatory when `targetPosition` is set to `FIXED`, but can be used also in the other two cases to set further boundaries.
It is represented by an object like `{"min": 1, "max": 2}` (if min or max is missing, default values are used)

* `targetDirection`: mandatory when `targetPosition` is set to `STRAIGHT`, ignored in all other cases. It specifies how to
compute the right direction. Supported values are:
    * `FIXED`: the target is in a fixed direction. It can be paired with `targetDistance` to limit how far in that direction
    the target can be
    * `INHERIT`: the direction must be inherited from the last used target calculator. It can be paired with `targetDistance` to limit how far in that direction
    the target can be
    
* `goesThroughWalls`: `true` if the direction does not stop at the first wall. `false` is default if the field is not specified

* `targetFinalPosition`: like target position, but used to determine the available the destination blocks for a move. Supported values are `FIXED` and `STRAIGHT`

* `targetFinalDestination`: like target destination, but used to determine the available destination blocks for a move

* `veto`: used to remove targets from the potential targets computed using the previous fields. Supported values:
    * `LAST_HIT`: the last target of this weapon (active player excluded)
    * `ALL_PREVIOUS`: all the targets hit by this weapon
    * `ALL_PREVIOUS_BLOCKS`: the blocks of all the targets that were previously hit
    * `HIT_BY_ADVANCED`: all the targets that were hit by the advanced attacks (so it only makes sense to have this value _if_ an advanced attack is present)
    