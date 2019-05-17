package person.crud.Model

import grails.gorm.annotation.Entity

/**
 * Created by chris on 17/05/19.
 */

@Entity
class Person {
    String firstName
    String lastName
    Integer age

    static constraints = {
        firstName blank: false, minSize: 5 , maxSize: 50
        lastName  blank: false, minSize: 5, maxSize: 50
        age       blank: true, matches: "[\\d{1,3}]"
    }
}
