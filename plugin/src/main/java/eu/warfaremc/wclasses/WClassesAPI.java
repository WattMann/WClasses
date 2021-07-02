package eu.warfaremc.wclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface WClassesAPI
{
    /**
     * Returns HeroObject from cache or database, this also caches the result for future use, wrapped in Optional container for ease of flow control
     *
     * @param uid {@link UUID} Unique ID of the hero object, tied to player unique ID
     * @return {@link Optional<HeroObject>}
     */
    @NotNull Optional<HeroObject> get(@Nullable UUID uid);

    /**
     * Pulls a HeroObject from cache and stores it in the database.
     *
     * @param uid {@link UUID} Unique ID of the hero object, tied to player unique ID
     * @return boolean true if object was successfully saved, false otherwise
     */
    boolean put(@Nullable UUID uid);

    record HeroObject(UUID uid, HeroClass heroClass) {
         public UUID uniqueID() {
             return uid;
         }
         @Override
         public HeroClass heroClass() {
             return heroClass;
         }

         public enum HeroClass {
            WARRIOR, ARCHER, SNIPER, PALADIN, NECROMANCER
        }
    }
}
