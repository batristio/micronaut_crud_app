package person.crud.Model

import grails.gorm.annotation.Entity

/**
 * Created by chris on 17/05/19.
 */

//ToDo: export Intellij's project setting from work

@Entity
class Person {
    Long id
    String firstName
    String lastName
    Integer age

    static constraints = {
        firstName blank: false, minSize: 2 , maxSize: 50
        lastName  blank: false, minSize: 2, maxSize: 50
        age       blank: true, matches: "[\\d{1,3}]"
    }
}
