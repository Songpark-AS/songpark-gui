(ns web-app.components.mobile-navbar)

(defn mobile-navbar-item [{:keys [icon-component title on-click active?]}]
  [:div {:class (if active? "item active" "item") :key (str "mobile-navbar-item-" title) :on-click on-click}
   (when (not (nil? icon-component))
     [:div.icon [:> icon-component]])
   [:div.title title]])

(defn mobile-navbar [{:keys [navbar-items]}]
  [:div.mobile-navbar
   (map mobile-navbar-item navbar-items)])
