package com.cts.service;


import org.springframework.stereotype.Service;

import com.cts.entity.SupplierOrder;
import com.cts.exception.InvalidRequestException;
import com.cts.repository.SupplierOrderRepository;
import com.cts.repository.SupplierRepository;

import java.util.List;

@Service
public class SupplierOrderService {

    private final SupplierOrderRepository orderRepo;
    private final SupplierRepository supplierRepo;

    public SupplierOrderService(
            SupplierOrderRepository orderRepo,
            SupplierRepository supplierRepo) {
        this.orderRepo = orderRepo;
        this.supplierRepo = supplierRepo;
    }

    public SupplierOrder placeOrder(SupplierOrder order) {

        if (!supplierRepo.existsById(order.getSupplierId())) {
            throw new InvalidRequestException(
                    "Supplier with ID " + order.getSupplierId() + " does not exist");
        }

        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new InvalidRequestException("Order quantity must be >= 1");
        }

        order.setStatus("PLACED");
        return orderRepo.save(order);
    }

    public List<SupplierOrder> getAllOrders() {
        return orderRepo.findAll();
    }
}
