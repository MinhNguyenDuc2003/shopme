package com.shopme.checkout;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shopme.common.entity.CartItem;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.ShippingRate;

@Service
public class CheckoutService {
	public static final int DIM_DIVISOR = 139;

	public CheckoutInfo prepareCheckout(List<CartItem> cartItems, ShippingRate shippingRate) {
		CheckoutInfo checkoutInfo = new CheckoutInfo();

		float productCost = calculateProductCost(cartItems);
		float ProductTotal = calculateProductTotal(cartItems);
		float shippingCostTotal = calculateShippingCost(cartItems, shippingRate);
		float paymentTotal = ProductTotal + shippingCostTotal;
		
		checkoutInfo.setProductCost(productCost);
		checkoutInfo.setProductTotal(ProductTotal);
		checkoutInfo.setDeliverDays(shippingRate.getDays());
		checkoutInfo.setCodSupported(shippingRate.isCodSupported());
		checkoutInfo.setShippingCostTotal(shippingCostTotal);
		checkoutInfo.setPaymentTotal(paymentTotal);
		return checkoutInfo;
	}

	public float calculateShippingCost(List<CartItem> cartItems, ShippingRate shippingRate) {
		float shippingCostTotal = 0.0F;

		for (CartItem item : cartItems) {

			Product product = item.getProduct();

			float dimWeight = (product.getLength() * product.getWidth() * product.getHeight()) / DIM_DIVISOR;
			float finalWeight = product.getWeight() > dimWeight ? product.getWeight() : dimWeight;
			float shippingCost = finalWeight * shippingRate.getRate()*item.getQuantity();
			item.setShippingCost(shippingCost);
			shippingCostTotal += shippingCost;
		}
		return shippingCostTotal;
	}

	private float calculateProductTotal(List<CartItem> cartItems) {
		float cost = 0.0F;

		for (CartItem item : cartItems) {
			cost += item.getSubtotal();
		}

		return cost;
	}

	private float calculateProductCost(List<CartItem> cartItems) {
		float cost = 0.0F;

		for (CartItem item : cartItems) {
			cost += item.getQuantity() * item.getProduct().getCost();
		}

		return cost;
	}

}
