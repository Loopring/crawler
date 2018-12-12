package org.loopring.crawler.util;

import org.loopring.crawler.models.BasicModel;

public class RepoUtils {
    // public static <T extends BasicModel> CrawlRepo<T> createRepoFromCrudRepo(final CrawlerCrudRepo<T> crudRepo) {
    //     return new CrawlRepo<T>() {
    //         @Override
    //         public void save(T t) {
    //             crudRepo.save(t);
    //         }

    //         @Override
    //         public boolean exists(T t) {
    //             if (t == null) return true;
    //             else {
    //                 long count = crudRepo.countByUuid(t.getUuid());
    //                 return count > 0;
    //             }
    //         }
    //     };
    // }

    public static <T extends BasicModel> boolean exists(CrawlerCrudRepo<T> repo, T data) {

        if (data == null || repo == null)
            return true;
        long count = repo.countByUuid(data.getUuid());
        return count > 0;
    }

    // public static <T extends Link> boolean linkExists(CrawlerCrudRepo<T> repo, T link) {

    //     if (link == null || repo == null)
    //         return true;
    //     long count = repo.countByUuid(link.getUuid());
    //     return count > 0;
    // }

}
