package cn.e3mall.cart.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.util.E3Result;
import cn.e3mall.common.util.JsonUtils;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.pojo.TbItemExample.Criteria;
import cn.e3mall.pojo.TbUser;

/**
 * 购物车处理服务
 * 
 * @author 2364201071
 *
 */
@Service
public class CartServiceImpl implements CartService {
	@Autowired
	private JedisClient jedisClient;
	@Autowired
	private TbItemMapper itemMapper;
	@Value("${REDIS_CART_PRE}")
	private String REDIS_CART_PRE;

	@Override
	// 添加购物车
	public E3Result addCart(long userId, long itemId, int num) {
		// 向redis中添加购物车
		// 数据类型是hash key:用户id field：商品id value:商品信息
		// 判断商品是否存在
		Boolean hexists = jedisClient.hexists(REDIS_CART_PRE + ":" + userId, itemId + "");
		// 若存在，数量相加
		if (hexists) {
			String json = jedisClient.hget(REDIS_CART_PRE + ":" + userId, itemId + "");
			TbItem item = JsonUtils.jsonToPojo(json, TbItem.class);
			item.setNum(item.getNum() + num);
			jedisClient.hset(REDIS_CART_PRE + ":" + userId, itemId + "", JsonUtils.objectToJson(item));
			return E3Result.ok();
		}
		// 若不存在，根据商品id取商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		// 设置购物车数量
		item.setNum(num);
		// 取一张图片
		String image = item.getImage();
		if (StringUtils.isNoneBlank(image)) {
			item.setImage(image.split(",")[0]);
		}
		// 添加到购物车列表
		jedisClient.hset(REDIS_CART_PRE + ":" + userId, itemId + "", JsonUtils.objectToJson(item));
		// 返回成功
		return E3Result.ok();
	}

	@Override
	// 将cookie中的购物车与服务端的购物车合并
	public E3Result mergeCat(long userId, List<TbItem> itemList) {
		// 遍历商品列表
		// 把列表添加到购物车
		// 判断购物车是否有此商品
		// 若有，数量相加
		// 若无，添加新的商品
		for (TbItem tbItem : itemList) {
			addCart(userId, tbItem.getId(), tbItem.getNum());
		}

		// 返回成功
		return E3Result.ok();
	}

	@Override
	// 取购物车列表
	public List<TbItem> getCartList(long userId) {
		List<TbItem> itemList = new ArrayList<>();
		List<String> jsonList = jedisClient.hvals(REDIS_CART_PRE + ":" + userId);
		for (String string : jsonList) {
			TbItem item = JsonUtils.jsonToPojo(string, TbItem.class);
			itemList.add(item);
		}
		return itemList;
	}

	@Override
	// 更新购物车商品数量
	public E3Result updateCartNum(long userId, long itemId, int num) {
		// 从redis中取商品信息
		String json = jedisClient.hget(REDIS_CART_PRE + ":" + userId, itemId + "");
		TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
		// 更新购物车商品数量
		tbItem.setNum(num);
		// 写入redis
		jedisClient.hset(REDIS_CART_PRE + ":" + userId, itemId + "", JsonUtils.objectToJson(tbItem));

		return E3Result.ok();
	}

	@Override
	// 删除购物车商品
	public E3Result deleteCartItem(long userId, long itemId) {
		jedisClient.hdel(REDIS_CART_PRE + ":" + userId, itemId + "");
		return E3Result.ok();
	}

	@Override
	//清空购物车
	public E3Result clearCartItem(long userId) {
		jedisClient.del(REDIS_CART_PRE + ":" + userId);
		return E3Result.ok();
	}

}
