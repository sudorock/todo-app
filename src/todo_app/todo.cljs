(ns todo-app.todo
 (:require [reagent.core :as r]))

(defonce todos (r/atom (sorted-map)))
(defonce counter (r/atom 0))
;
(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :text text :done false :notes "hello" :due-date ""})))

(defn toggle-todo [id] (swap! todos update-in [id :done] not))

(defn toggle-all []
  [:button "Toggle All"])

(defn todo-input []
  (let [input (r/atom "")]
    (fn []
      [:form
       [:input {:type "text" :value @input :on-change #(reset! input (-> % .-target .-value))}] 
       [:button {:on-click (fn [e] (.preventDefault e) (add-todo @input) (reset! input ""))} "Add"]])))

(defn todo-item [{:keys [id text done notes due-date]}]
  (let [is-editing (r/atom false)
        edit-input (r/atom text) 
        show-date-input (r/atom false) 
        edit-date (r/atom due-date)
        show-notes-input (r/atom false)
        edit-notes (r/atom notes)]
    (fn [{:keys [id text done notes due-date]}]
      [:div {:id (.toString id)}
       (if-not @is-editing
         [:<>
          [:button {:on-click (fn [e] (.preventDefault e) (toggle-todo id))} (if done "@  " "X  ")]
          [:span text]
          [:span due-date]
          [:button {:on-click (fn [e] (reset! is-editing true))} "Edit"]
          [:button {:on-click (fn [e] (swap! todos dissoc id))} "Delete"]
          [:button {:on-click (fn [e] (reset! show-date-input true))} "Due Date"]
          [:button {:on-click (fn [e] (reset! show-notes-input true))} "Notes"]
          (when @show-date-input [:form 
                                  [:input {:type "date" :value @edit-date :on-change #(reset! edit-date (-> % .-target .-value))}]
                                  [:button {:on-click (fn [e] 
                                                        (.preventDefault e)
                                                        (swap! todos assoc-in [id :due-date] @edit-date)
                                                        (reset! show-date-input false))} "OK"]])
          (when @show-notes-input [:form
                                  [:textarea {:on-change #(reset! edit-notes (-> % .-target .-value))} @edit-notes]
                                  [:button {:on-click (fn [e]
                                                        (.preventDefault e)
                                                        (swap! todos assoc-in [id :notes] @edit-notes)
                                                        (reset! show-notes-input false))} "OK"]])]
         [:<>
          [:form
           [:input {:type "text" :value @edit-input :on-change #(reset! edit-input (-> % .-target .-value))}]
           [:button {:on-click (fn [e]
                                 (.preventDefault e)
                                 (reset! is-editing false)
                                 (swap! todos assoc-in [id :text] @edit-input))} "OK"]
           [:button {:on-click (fn [e] (.preventDefault e) (reset! is-editing false))} "Cancel"]]])])))

(defn todo-app []
  [:div
   [todo-input]
   (for [todo (vals @todos)] ^{:key (:id todo)} [todo-item todo])])

(defn mount-root []
  (r/render [todo-app] (.getElementById js/document "app")))