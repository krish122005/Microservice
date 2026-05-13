package com.cts.controller;

import org.springframework.web.bind.annotation.*;

import com.cts.entity.SupplierOrder;
import com.cts.service.SupplierOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class SupplierOrderController {

    private final SupplierOrderService orderService;

    public SupplierOrderController(SupplierOrderService orderService) {
        this.orderService = orderService;
    }

    //@PreAuthorize("hasAnyRole('Admin','PROCUREMENT')")
    @GetMapping
    public List<SupplierOrder> listOrders() {
        return orderService.getAllOrders();
    }

    //@PreAuthorize("hasAnyRole('Admin', 'PROCUREMENT')")
    @PostMapping
    public SupplierOrder createOrder(@RequestBody SupplierOrder order) {
        return orderService.placeOrder(order);
    }
}