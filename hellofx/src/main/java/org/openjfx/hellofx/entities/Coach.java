package org.openjfx.hellofx.entities;

import java.util.Set;
import org.openjfx.hellofx.entities.Specialization;

public record Coach(Long id, String name, String email, String phoneNumber,
                    Set<Specialization> specializations){

}
