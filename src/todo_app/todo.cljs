(ns todo-app.todo
 (:require [reagent.core :as r]))

(defonce todos (r/atom (sorted-map)))
(defonce counter (r/atom 0))
;
(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :text text :done false})))
;
;(defn toggle [id] (swap! todos update-in [id :done] not))
;
(defn todo-input []
  (let [input (r/atom "")]
    (fn []
      [:form
       [:input {:type "text" :value @input :on-change #(reset! input (-> % .-target .-value))}] 
       [:button {:on-click (fn [e] (.preventDefault e) (add-todo @input) (reset! input ""))} "Add"]])))

(defn todo-item [{:keys [id text done]}]
(let [editing (r/atom false)]
  [:div {:id (.toString id)}
   [:span (if done "@  " "X  ")]
   [:span text]
   [:button {:on-click (fn [e] ())} "Edit"]
   [:button {:on-click (fn [e] (swap! todos dissoc id))} "Delete"]]))

(defn todo-app []
  [:div
   [todo-input]
   (for [todo (vals @todos)] ^{:key (:id todo)} [todo-item todo])])

(defn app []
  [:div [todo-input]]) 

(defn mount-root []
  (r/render [todo-app] (.getElementById js/document "app")))