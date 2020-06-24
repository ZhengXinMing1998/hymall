package cn.e3mall.cart.service;

import java.util.List;

import cn.e3mall.common.util.E3Result;
import cn.e3mall.pojo.TbItem;

public interface CartService {
	//添加购物车
	E3Result addCart(long userId,long itemId,int num);
	//将cookie中的购物车与服务端的购物车合并
	E3Result mergeCat(long userId,List<TbItem> itemList);
	//取购物车列表
	List<TbItem> getCartList(long userId);
	//更新购物车商品数量
	E3Result updateCartNum(long userId, long itemId, int num);
	// 删除购物车商品
	E3Result deleteCartItem(long userId, long itemId);
	//清空购物车
	E3Result clearCartItem(long userId);
}
