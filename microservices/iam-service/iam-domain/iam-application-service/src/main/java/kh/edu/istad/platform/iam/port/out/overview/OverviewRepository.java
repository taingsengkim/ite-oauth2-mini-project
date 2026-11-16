package kh.edu.istad.platform.iam.port.out.overview;

public interface OverviewRepository {
    record Statistics(long totalUsers, long activeUsers, long disabledUsers, long lockedUsers,
                      long roles, long permissions, long clients) {}
    Statistics statistics();
}
