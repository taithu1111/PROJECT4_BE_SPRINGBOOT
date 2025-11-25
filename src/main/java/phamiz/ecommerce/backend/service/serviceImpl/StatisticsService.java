package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.repositories.IOrderRepository;
import phamiz.ecommerce.backend.repositories.IProductRepository;
import phamiz.ecommerce.backend.repositories.UserRepository;
import phamiz.ecommerce.backend.service.IStatisticsService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService implements IStatisticsService {

    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Long totalRevenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.countOrders();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        stats.put("totalOrders", totalOrders != null ? totalOrders : 0);
        stats.put("totalProducts", totalProducts);
        stats.put("totalUsers", totalUsers);

        return stats;
    }
}
