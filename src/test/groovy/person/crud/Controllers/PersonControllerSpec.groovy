package person.crud.Controllers

import grails.gorm.transactions.Rollback
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.http.client.RxHttpClient
import person.crud.Model.Person
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@Rollback
class PersonControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def cleanup() {
        Person.list().each { person ->
            deletePerson(person.id)
        }
    }

    void "test index"() {
        when:
        HttpResponse response = client.toBlocking().exchange("/person")

        then:
        response.status == HttpStatus.OK
    }

    void "test empty"() {
        when:
        List list = client.toBlocking().retrieve("/person/list", List)

        then:
        list.size() == 0
    }

    void "test save a person"() {
        when:
        HttpResponse response = savePerson(new Person(firstName: 'John', lastName: 'Doe', age: 18))
        List list = client.toBlocking().retrieve("/person/list", List)

        then:
        response.status == HttpStatus.OK
        list.size() == 1
    }

    void "test save person throws error"() {
        when:
        savePerson(new Person(firstName: 'asd'))

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.message == "Unprocessable Entity"
    }

    void "test get a person"() {
        when:
        HttpResponse response = savePerson(new Person(firstName: 'John', lastName: 'Doe', age: 18))
        List list = client.toBlocking().retrieve("/person/get/" + getFirstPersonId(), List)

        then:
        response.status == HttpStatus.OK
        list.size() == 1
    }

    // ToDo: when throw custom error message
    void "test get person throws error"() {
        when:
        client.toBlocking().exchange(HttpRequest.GET("/person/get/1"))

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    void "test delete a person"() {
        when:
        savePerson(new Person(firstName: 'John', lastName: 'Doe', age: 18))
        HttpResponse response = deletePerson(getFirstPersonId())
        List list = client.toBlocking().retrieve("/person/list", List)

        then:
        response.status == HttpStatus.OK
        list.size() == 0
    }

    // ToDo: when no person is found then raise exception
    void "test delete a non existent person"() {
        when:
        HttpResponse response = deletePerson(999)

        then:
        response.status == HttpStatus.OK
    }

    void "test save many people"() {
        when:
        HttpResponse response = savePerson([
                new Person(firstName: "fname", lastName: "lname", age: 19),
                new Person(firstName: "fname2", lastName: "lname2", age: 20)
        ])
        List list = client.toBlocking().retrieve("/person/list", List)

        then:
        response.status == HttpStatus.OK
        list.size() == 2
    }

    void "test save many people throws error"() {
        when:
        savePerson([
                new Person(firstName: "fname", lastName: "lname", age: 19),
                new Person(firstName: "fname2", lastName: "", age: 20)
        ])

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
    }

    void "test count method"() {
        when:

        HttpResponse response = savePerson([
            new Person(firstName: "fname", lastName: "lname", age: 19),
            new Person(firstName: "fname2", lastName: "lname2", age: 20)
        ])

        then:
        response.status == HttpStatus.OK

        when:
        Integer count = client.toBlocking().retrieve("/person/count", Integer)

        then:
        count == 2

        when:
        response = deletePerson(getFirstPersonId())
        count = client.toBlocking().retrieve("/person/count", Integer)

        then:
        response.status == HttpStatus.OK
        count == 1

        when:
        response = deletePerson(getFirstPersonId())
        count = client.toBlocking().retrieve("/person/count", Integer)

        then:
        response.status == HttpStatus.OK

        and:
        count == 0
    }

    void "test update method"() {
        when:
        HttpResponse response = savePerson(new Person(firstName: "fname", lastName: "lname", age: 19))

        then:
        response.status == HttpStatus.OK

        when:
        Person person = Person.first()
        person.firstName = "fnameUpdated"
        response = client.toBlocking().exchange(HttpRequest.PATCH("/person/update", person))

        then:
        response.status == HttpStatus.OK

        when:
        response = client.toBlocking().exchange("/person/list")
        List list = client.toBlocking().retrieve("/person/list", List)

        then:
        response.status == HttpStatus.OK
        list.size() == 1

        and:
        Person.first().firstName == "fnameUpdated"
    }

    void "test update method throws error"() {
        when:
        HttpResponse response = savePerson(new Person(firstName: "fname", lastName: "lname", age: 19))

        then:
        response.status == HttpStatus.OK

        when:
        Person person = Person.first()
        person.firstName = ""
        response = client.toBlocking().exchange(HttpRequest.PATCH("/person/update", person))

        then:
        def e = thrown(HttpClientResponseException)
        e.status
    }

    private static def getFirstPersonId() {
        return Person?.list()?.get(0)?.id
    }

    private HttpResponse deletePerson(Long id) {
        return client.toBlocking().exchange(HttpRequest.DELETE("/person/delete/${id}"))
    }

    private HttpResponse savePerson(Person person) {
        return client.toBlocking().exchange(HttpRequest.PUT("/person/save", person))
    }

    private HttpResponse savePerson(List<Person> people) {
        HttpResponse response = HttpResponse.unprocessableEntity()
        people.each {
            response = savePerson(it)
        }
        return response
    }
}
