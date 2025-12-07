import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, detail } from '../api'
import { useUserStore } from './user'

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
        if (!userStore.userInfo) return

        loading.value = true
        try {
            const r = await getCart(userStore.userInfo.id)
            if (r.data.code === 0) {
                const items = r.data.data
                // 补全信息
                for (let item of items) {
                    try {
                        const p = await detail(item.prodId)
                        if(p.data.code===0) {
                            item.prodTitle = p.data.data.title
                            item.prodPrice = p.data.data.price
                            item.prodImage = p.data.data.images?.split(',')[0]
                            item.selected = false // 默认不选中
                        }
                    } catch(e){}
                }
                cartList.value = items
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