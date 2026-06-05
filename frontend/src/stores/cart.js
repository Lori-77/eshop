import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, addToCart, updateCartQuantity, updateCartSelected, removeCartItem, clearCart } from '../api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalCount = computed(() => items.value.filter(i => i.selected).reduce((sum, i) => sum + i.quantity, 0))
  const totalAmount = computed(() => items.value.filter(i => i.selected).reduce((sum, i) => sum + i.price * i.quantity, 0))

  async function fetchCart() {
    try {
      const res = await getCart()
      items.value = res.data || []
    } catch {
      items.value = []
    }
  }

  async function add(productId, quantity = 1) {
    await addToCart({ productId, quantity })
    await fetchCart()
  }

  async function updateQty(cartItemId, quantity) {
    await updateCartQuantity(cartItemId, quantity)
    await fetchCart()
  }

  async function toggleSelected(cartItemId, selected) {
    await updateCartSelected(cartItemId, selected)
    await fetchCart()
  }

  async function remove(cartItemId) {
    await removeCartItem(cartItemId)
    await fetchCart()
  }

  async function clear() {
    await clearCart()
    items.value = []
  }

  return { items, totalCount, totalAmount, fetchCart, add, updateQty, toggleSelected, remove, clear }
})
