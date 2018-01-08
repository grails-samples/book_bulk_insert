package demo

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.hibernate.SessionFactory

@CompileStatic
class BookCleanupGormService implements BookGateway {

    SessionFactory sessionFactory

    @Transactional
    @Override
    void importBooksInLibrary(Library library) {
        library.eachWithIndex { Object bookValueMap, int index ->
            updateOrInsertBook(bookValueMap as Map)
            if (index % 100 == 0) cleanUpGorm()
        }
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }

    void updateOrInsertBook(Map bookValueMap) {
        String title = bookValueMap.title
        String isbnValue = bookValueMap.isbn
        String editionValue = bookValueMap.edition
        Book existingBook = Book.where {
            isbn == isbnValue && edition == editionValue
        }.get()

        if (existingBook) { // just update title
            existingBook.title = title
            existingBook.save()
        } else {
            new Book(bookValueMap).save()
        }
    }
}