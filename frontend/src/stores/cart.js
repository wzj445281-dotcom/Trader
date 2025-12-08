import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, detail } from '../api'
import { useUserStore } from './user'
import { ElMessage } from 'element-plus' // 引入 ElMessage 用于错误提示

export const useCartStore = defineStore('cart', () => {
    const cartList = ref([])
    const loading = ref(false)

    // 对应报告：getters 计算总价
    const totalPrice = computed(() => {
        return cartList.value
            .filter(item => item.selected) // 仅计算选中的
            .reduce((sum, item) => sum + (item.prodPrice || 0) * item.qty, 0)
            .toFixed(2)
    })

    // 对应报告：getters 计算总数
    const totalCount = computed(() => cartList.value.length)

    // 对应报告：actions 拉取数据
    async function fetchCart() {
        const userStore = useUserStore()
        if (!userStore.userInfo) {
            cartList.value = []
            return
        }

        loading.value = true
        try {
            // 1. 获取购物车列表 - 🔥 直接调用，后端从 JWT 获取用户 ID
            const r = await getCart()
            if (r.data.code === 0) {
                const items = r.data.data
                const cartItemsWithDetails = []

                // 2. 批量获取商品详情
                // 使用 Promise.allSettled 确保所有请求都完成，即使有失败的
                const detailPromises = items.map(item => detail(item.prodId));
                const results = await Promise.allSettled(detailPromises);

                for (let i = 0; i < items.length; i++) {
                    const item = items[i]
                    const detailResult = results[i]

                    // 默认值，防止渲染错误
                    item.prodTitle = '商品信息加载失败'
                    item.prodPrice = 0.00
                    item.prodImage = ''
                    item.selected = false // 默认不选中

                    if (detailResult.status === 'fulfilled' && detailResult.value.data.code === 0) {
                        const p = detailResult.value.data.data;
                        item.prodTitle = p.title
                        item.prodPrice = p.price
                        item.prodImage = p.images?.split(',')[0]
                    } else if (detailResult.status === 'rejected' || detailResult.value.data.code !== 0) {
                        console.error('Failed to fetch product detail:', item.prodId, detailResult.reason || detailResult.value.data.msg)
                        // 详情加载失败，仍将该项加入列表，但信息不完整
                        // 实际应该检查商品是否存在，如果不存在则删除购物车项
                    }
                    cartItemsWithDetails.push(item);
                }

                cartList.value = cartItemsWithDetails
            } else {
                ElMessage.error(r.data.msg || '获取购物车列表失败')
                cartList.value = []
            }
        } catch(e){
            console.error('Error fetching cart:', e)
            // 检查是否是 403/401 错误，通常是权限问题
            if (e.response && (e.response.status === 401 || e.response.status === 403)) {
                ElMessage.error('获取购物车失败：登录状态失效，请重新登录。')
                userStore.logout() // 触发登出
            } else {
                ElMessage.error('获取购物车数据失败，请检查后端服务。')
            }
        } finally {
            loading.value = false
        }
    }

    function clearSelection() {
        cartList.value.forEach(i => i.selected = false)
    }

    return { cartList, loading, totalPrice, totalCount, fetchCart, clearSelection }
})