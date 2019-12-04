(ns todo-app.todo
 (:require [reagent.core :as r]))

;(defonce todos (r/atom (sorted-map)))
;(defonce counter (r/atom 0))
;
;(defn add-todo [text]
;      (let [id (swap! counter inc)]
;           (swap! todos assoc id {:id id :text text :done false})))
;
;(defn toggle [id] (swap! todos update-in [id :done] not))
;
;(defn todo-input []
;      [:input {:type "text"}])
;
;(defn todo-item [{:keys [id text done]}])
;
;
;
;(defn todo-app []
;      [:div
;       [todo-input]])

(defn counting-button [txt]
  (let [state (r/atom 0)]
    (fn [txt]
      [:button.green {:on-click #(swap! state inc)} (str txt " " @state)])))

(defn mount-root []
  (r/render [counting-button] (.getElementById js/document "app")))

(mount-root)

(defn init []
  (js/console.log "Hello World Sunil"))