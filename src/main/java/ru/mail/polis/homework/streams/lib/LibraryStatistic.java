package ru.mail.polis.homework.streams.lib;

import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.util.stream.Collectors;

/**
 * Класс для работы со статистикой по библиотеке.
 * Оценка 5-ть баллов
 */
public class LibraryStatistic {

    private static int DAYS_TO_MILLISECONDS = 86400000;

    /**
     * Вернуть "специалистов" в литературном жанре с кол-вом прочитанных страниц.
     * Специалист жанра считается пользователь который прочел как минимум 5 книг в этом жанре,
     * при этом читал каждую из них не менее 14 дней.
     * @param library - данные библиотеки
     * @param genre - жанр
     * @return - map пользователь / кол-во прочитанных страниц
     */
    public Map<User, Integer> specialistInGenre(Library library, Genre genre) {
        return library.getArchive()
                .stream().filter(it -> it.getBook().getGenre()==genre)
                .collect(Collectors.groupingBy(ArchivedData::getUser)).entrySet().stream()
                .filter(it -> it.getValue().size() >= 5 && it.getValue().stream().allMatch(data ->
                        (data.getReturned() != null ?
                                data.getReturned().getTime() : new Timestamp(System.currentTimeMillis()).getTime() -
                                data.getTake().getTime()) >= 14L * DAYS_TO_MILLISECONDS))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        it -> it.getValue().stream().mapToInt(value -> value.getBook().getPage()).sum()));
    }

    /**
     * Вернуть любимый жанр пользователя. Тот что чаще всего встречается. Не учитывать тот что пользователь читает в данный момент.
     * Если есть несколько одинаковых по весам жанров - брать в расчет то, что пользователь читает в данный момент.
     * @param library - данные библиотеки
     * @param user - пользователь
     * @return - жанр
     */
    public Genre loveGenre(Library library, User user) {
        return library.getArchive().stream().filter(it -> it.getUser() == user).collect(Collectors.groupingBy(it ->
                it.getBook().getGenre())).entrySet().stream().max((first, second) -> {
                    if (first.getValue().stream().filter(data -> data.getReturned() != null).count()
                            == second.getValue().stream().filter(data -> data.getReturned() != null).count()) {
                        return first.getValue().size() - second.getValue().size();
                    }
                    return (int) (first.getValue().stream().filter(data ->
                            data.getReturned() != null).count() - second.getValue().stream().filter(data ->
                            data.getReturned() != null).count());
        }).get().getKey();
    }

    /**
     * Вернуть список пользователей которые больше половины книг держали на руках более 30-ти дней. Брать в расчет и книги которые сейчас
     * пользователи держат у себя (ArchivedData.returned == null)
     * @param library - данные библиотеки
     * @return - список ненадежных пользователей
     */
    public List<User> unreliableUsers(Library library) {
        return library.getUsers().stream().filter(user -> (double) library.getArchive().stream().filter(it ->
                (it.getReturned() != null ?
                        it.getReturned().getTime() : new Timestamp(System.currentTimeMillis()).getTime() -
                        it.getTake().getTime()) > 30L * DAYS_TO_MILLISECONDS && it.getUser() == user).count() /
                        (double) library.getArchive().stream().filter(it -> it.getUser() == user).count() > 0.5
                )
                .collect(Collectors.toList());
    }

    /**
     * Вернуть список книг у которых страниц равно или больше чем переданное значение
     * @param library - данные библиотеки
     * @param countPage - кол-во страниц
     * @return - список книг
     */
    public List<Book> booksWithMoreCountPages(Library library, int countPage) {
        return library.getBooks().stream().filter(it -> it.getPage() >= countPage).collect(Collectors.toList());
    }

    /**
     * Вернуть самого популярного автора в каждом жанре. Если кол-во весов у авторов одинаково брать по алфавиту.
     * @param library - данные библиотеки
     * @return - map жанр / самый популярный автор
     */
    public Map<Genre, String> mostPopularAuthorInGenre(Library library) {
        // Под "самым популярным автором в каждом жанре" я понимаю автора, книги которого брали чаще всего
        return library.getArchive().stream().collect(Collectors.groupingBy(it -> it.getBook().getGenre()))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        it -> it.getValue().stream().collect(Collectors.groupingBy(data ->
                                data.getBook().getAuthor())).entrySet().stream()
                                .max((first, second) -> {
                                    if (first.getValue().size() == second.getValue().size()) {
                                        return first.getKey().compareTo(second.getKey());
                                    }
                                    return first.getValue().size() - second.getValue().size();
                                }).get().getKey()));
    }
}
